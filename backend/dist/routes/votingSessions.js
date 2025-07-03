"use strict";
var __importDefault = (this && this.__importDefault) || function (mod) {
    return (mod && mod.__esModule) ? mod : { "default": mod };
};
Object.defineProperty(exports, "__esModule", { value: true });
const express_1 = __importDefault(require("express"));
const VotingSession_1 = __importDefault(require("../models/VotingSession"));
const Group_1 = __importDefault(require("../models/Group"));
const User_1 = __importDefault(require("../models/User"));
const axios_1 = __importDefault(require("axios"));
const firebase_admin_1 = __importDefault(require("firebase-admin"));
const router = express_1.default.Router();
// Helper: fetch movies from TMDB based on genres
async function fetchMoviesByGenres(genres, count = 20) {
    // You may want to cache genre IDs from TMDB
    const genreString = genres.join(',');
    const url = `https://api.themoviedb.org/3/discover/movie?api_key=${process.env.TMDB_API_KEY}&with_genres=${genreString}&sort_by=popularity.desc`;
    const response = await axios_1.default.get(url);
    return response.data.results.slice(0, count);
}
// Helper: send FCM notification to a list of tokens
async function sendFCMNotification(tokens, title, body) {
    if (!firebase_admin_1.default.apps.length) {
        firebase_admin_1.default.initializeApp({
            credential: firebase_admin_1.default.credential.applicationDefault(),
        });
    }
    if (tokens.length === 0)
        return;
    await firebase_admin_1.default.messaging().sendMulticast({
        tokens,
        notification: { title, body },
    });
}
// POST /voting-sessions/start
router.post('/start', async (req, res) => {
    const { groupId } = req.body;
    if (!groupId)
        return res.status(400).json({ error: 'Missing groupId' });
    try {
        const group = await Group_1.default.findById(groupId).populate('members');
        if (!group)
            return res.status(404).json({ error: 'Group not found' });
        // Collect all members' genre preferences
        const users = await User_1.default.find({ _id: { $in: group.members } });
        const allGenres = users.flatMap(u => u.genrePreferences);
        // Count genre frequency
        const genreCounts = {};
        allGenres.forEach(g => { genreCounts[g] = (genreCounts[g] || 0) + 1; });
        // Sort genres by popularity
        const sortedGenres = Object.entries(genreCounts)
            .sort((a, b) => b[1] - a[1])
            .map(([g]) => g);
        // Pick top 3 genres for the group
        const topGenres = sortedGenres.slice(0, 3);
        // Fetch movies from TMDB
        const movies = await fetchMoviesByGenres(topGenres);
        // Create voting session
        const votingSession = new VotingSession_1.default({
            group: groupId,
            movies: movies.map(m => ({
                tmdbId: m.id,
                title: m.title,
                genres: m.genre_ids.map(String),
                posterPath: m.poster_path,
            })),
            votes: [],
            isActive: true,
        });
        await votingSession.save();
        // Send FCM notification to all group members (except the initiator if desired)
        const tokens = users.map(u => u.fcmToken).filter(Boolean);
        await sendFCMNotification(tokens, 'Voting Session Started', 'A new movie voting session has started!');
        res.json({ sessionId: votingSession._id, movies: votingSession.movies });
    }
    catch (err) {
        res.status(500).json({ error: 'Failed to start voting session' });
    }
});
// POST /voting-sessions/vote
router.post('/vote', async (req, res) => {
    const { sessionId, userId, movieId, vote } = req.body;
    if (!sessionId || !userId || !movieId || !vote)
        return res.status(400).json({ error: 'Missing fields' });
    try {
        const session = await VotingSession_1.default.findById(sessionId);
        if (!session || !session.isActive)
            return res.status(404).json({ error: 'Session not found or inactive' });
        // Remove previous vote by this user for this movie
        session.votes = session.votes.filter(v => !(v.user.equals(userId) && v.movieId === movieId));
        session.votes.push({ user: userId, movieId, vote });
        await session.save();
        res.json({ success: true });
    }
    catch (err) {
        res.status(500).json({ error: 'Failed to submit vote' });
    }
});
// POST /voting-sessions/end
router.post('/end', async (req, res) => {
    const { sessionId } = req.body;
    if (!sessionId)
        return res.status(400).json({ error: 'Missing sessionId' });
    try {
        const session = await VotingSession_1.default.findById(sessionId);
        if (!session || !session.isActive)
            return res.status(404).json({ error: 'Session not found or inactive' });
        session.isActive = false;
        session.endedAt = new Date();
        await session.save();
        // Compute winning movie (most yes votes)
        const yesVotes = {};
        session.votes.forEach(v => {
            if (v.vote === 'yes')
                yesVotes[v.movieId] = (yesVotes[v.movieId] || 0) + 1;
        });
        const winningMovieId = Object.entries(yesVotes).sort((a, b) => b[1] - a[1])[0]?.[0];
        const winningMovie = session.movies.find(m => m.tmdbId === Number(winningMovieId));
        // Send FCM notification to all group members
        const group = await Group_1.default.findById(session.group);
        const users = await User_1.default.find({ _id: { $in: group.members } });
        const tokens = users.map(u => u.fcmToken).filter(Boolean);
        await sendFCMNotification(tokens, 'Voting Session Ended', `The winning movie is: ${winningMovie?.title || 'No movie selected'}`);
        res.json({ winningMovie });
    }
    catch (err) {
        res.status(500).json({ error: 'Failed to end voting session' });
    }
});
exports.default = router;
