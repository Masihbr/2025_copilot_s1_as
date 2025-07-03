import express from 'express';
import { OAuth2Client } from 'google-auth-library';
import User from '../models/User';

const router = express.Router();

const client = new OAuth2Client(process.env.GOOGLE_CLIENT_ID);

// POST /auth/google
router.post('/google', async (req, res) => {
  const { idToken, fcmToken } = req.body;
  try {
    const ticket = await client.verifyIdToken({
      idToken,
      audience: process.env.GOOGLE_CLIENT_ID,
    });
    const payload = ticket.getPayload();
    if (!payload) return res.status(401).json({ error: 'Invalid Google token' });
    const { sub, name, email, picture } = payload;
    let user = await User.findOne({ googleId: sub });
    if (!user) {
      user = new User({
        googleId: sub,
        name,
        email,
        avatarUrl: picture,
        fcmToken,
        genrePreferences: [],
      });
      await user.save();
    } else {
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
  } catch (err) {
    res.status(401).json({ error: 'Google authentication failed' });
  }
});

export default router;

