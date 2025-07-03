"use strict";
var __importDefault = (this && this.__importDefault) || function (mod) {
    return (mod && mod.__esModule) ? mod : { "default": mod };
};
Object.defineProperty(exports, "__esModule", { value: true });
const express_1 = __importDefault(require("express"));
const google_auth_library_1 = require("google-auth-library");
const User_1 = __importDefault(require("../models/User"));
const router = express_1.default.Router();
const client = new google_auth_library_1.OAuth2Client(process.env.GOOGLE_CLIENT_ID);
// POST /auth/google
router.post('/google', async (req, res) => {
    const { idToken, fcmToken } = req.body;
    try {
        const ticket = await client.verifyIdToken({
            idToken,
            audience: process.env.GOOGLE_CLIENT_ID,
        });
        const payload = ticket.getPayload();
        if (!payload)
            return res.status(401).json({ error: 'Invalid Google token' });
        const { sub, name, email, picture } = payload;
        let user = await User_1.default.findOne({ googleId: sub });
        if (!user) {
            user = new User_1.default({
                googleId: sub,
                name,
                email,
                avatarUrl: picture,
                fcmToken,
                genrePreferences: [],
            });
            await user.save();
        }
        else {
            user.fcmToken = fcmToken;
            await user.save();
        }
        res.json({
            userId: user._id,
            name: user.name,
            email: user.email,
            avatarUrl: user.avatarUrl,
            genrePreferences: user.genrePreferences,
        });
    }
    catch (err) {
        res.status(401).json({ error: 'Google authentication failed' });
    }
});
exports.default = router;
