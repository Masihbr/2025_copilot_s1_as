import express from 'express';
import VotingSession from '../models/VotingSession';
import Group from '../models/Group';
import User from '../models/User';
import axios from 'axios';
import admin from 'firebase-admin';

const router = express.Router();

// Helper: fetch movies from TMDB based on genres
async function fetchMoviesByGenres(genres: string[], count: number = 20) {
  // You may want to cache genre IDs from TMDB
  const genreString = genres.join(',');
  const url = `https://api.themoviedb.org/3/discover/movie?api_key=${process.env.TMDB_API_KEY}&with_genres=${genreString}&sort_by=popularity.desc`;
  const response = await axios.get(url);
  return response.data.results.slice(0, count);
}

// Helper: send FCM notification to a list of tokens
async function sendFCMNotification(tokens: string[], title: string, body: string) {
  if (!admin.apps.length) {
    admin.initializeApp({
      credential: admin.credential.applicationDefault(),
    });
  }
  if (tokens.length === 0) return;
  await admin.messaging().sendMulticast({
    tokens,
    notification: { title, body },
  });
}

// POST /voting-sessions/start
router.post('/start', async (req, res) => {
  const { groupId } = req.body;
  if (!groupId) return res.status(400).json({ error: 'Missing groupId' });
  try {
    const group = await Group.findById(groupId).populate('members');
    if (!group) return res.status(404).json({ error: 'Group not found' });
    // Collect all members' genre preferences
    const users = await User.find({ _id: { $in: group.members } });
    const allGenres = users.flatMap(u => u.genrePreferences);
    // Count genre frequency
    const genreCounts: Record<string, number> = {};
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
    const votingSession = new VotingSession({
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
    const tokens = users.map(u => u.fcmToken).filter(Boolean) as string[];
    await sendFCMNotification(tokens, 'Voting Session Started', 'A new movie voting session has started!');
    res.json({ sessionId: votingSession._id, movies: votingSession.movies });
  } catch (err) {
    res.status(500).json({ error: 'Failed to start voting session' });
  }
});

// POST /voting-sessions/vote
router.post('/vote', async (req, res) => {
  const { sessionId, userId, movieId, vote } = req.body;
  if (!sessionId || !userId || !movieId || !vote) return res.status(400).json({ error: 'Missing fields' });
  try {
    const session = await VotingSession.findById(sessionId);
    if (!session || !session.isActive) return res.status(404).json({ error: 'Session not found or inactive' });
    // Remove previous vote by this user for this movie
    session.votes = session.votes.filter(v => !(v.user.equals(userId) && v.movieId === movieId));
    session.votes.push({ user: userId, movieId, vote });
    await session.save();
    res.json({ success: true });
  } catch (err) {
    res.status(500).json({ error: 'Failed to submit vote' });
  }
});

// POST /voting-sessions/end
router.post('/end', async (req, res) => {
  const { sessionId } = req.body;
  if (!sessionId) return res.status(400).json({ error: 'Missing sessionId' });
  try {
    const session = await VotingSession.findById(sessionId);
    if (!session || !session.isActive) return res.status(404).json({ error: 'Session not found or inactive' });
    session.isActive = false;
    session.endedAt = new Date();
    await session.save();
    // Compute winning movie (most yes votes)
    const yesVotes: Record<number, number> = {};
    session.votes.forEach(v => {
      if (v.vote === 'yes') yesVotes[v.movieId] = (yesVotes[v.movieId] || 0) + 1;
    });
    const winningMovieId = Object.entries(yesVotes).sort((a, b) => b[1] - a[1])[0]?.[0];
    const winningMovie = session.movies.find(m => m.tmdbId === Number(winningMovieId));
    // Send FCM notification to all group members
    const group = await Group.findById(session.group);
    const users = await User.find({ _id: { $in: group.members } });
    const tokens = users.map(u => u.fcmToken).filter(Boolean) as string[];
    await sendFCMNotification(tokens, 'Voting Session Ended', `The winning movie is: ${winningMovie?.title || 'No movie selected'}`);
    res.json({ winningMovie });
  } catch (err) {
    res.status(500).json({ error: 'Failed to end voting session' });
  }
});

export default router;
