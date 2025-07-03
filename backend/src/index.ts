import express from 'express';
import mongoose from 'mongoose';
import dotenv from 'dotenv';
import cors from 'cors';
import authRoutes from './routes/auth';
import groupRoutes from './routes/groups';
import votingSessionRoutes from './routes/votingSessions';

// Load environment variables from .env file
dotenv.config();

const app = express();
const PORT = process.env.PORT || 4000;

app.use(cors());
app.use(express.json());

// MongoDB connection
mongoose.connect(process.env.MONGODB_URI as string)
  .then(() => console.log('MongoDB connected'))
  .catch((err) => console.error('MongoDB connection error:', err));

// Health check endpoint
app.get('/', (req, res) => {
  res.send('MovieSwipe backend is running.');
});

app.use('/auth', authRoutes);
app.use('/groups', groupRoutes);
app.use('/voting-sessions', votingSessionRoutes);

app.listen(PORT, () => {
  console.log(`Server running on port ${PORT}`);
});
