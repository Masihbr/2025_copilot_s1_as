import mongoose, { Schema, Document, Types } from 'mongoose';

export interface IVotingSession extends Document {
  group: Types.ObjectId; // Group _id
  movies: {
    tmdbId: number;
    title: string;
    genres: string[];
    posterPath?: string;
  }[];
  votes: {
    user: Types.ObjectId; // User _id
    movieId: number; // tmdbId
    vote: 'yes' | 'no';
  }[];
  startedAt: Date;
  endedAt?: Date;
  isActive: boolean;
}

const VotingSessionSchema: Schema = new Schema({
  group: { type: Schema.Types.ObjectId, ref: 'Group', required: true },
  movies: [
    {
      tmdbId: { type: Number, required: true },
      title: { type: String, required: true },
      genres: [String],
      posterPath: { type: String },
    },
  ],
  votes: [
    {
      user: { type: Schema.Types.ObjectId, ref: 'User', required: true },
      movieId: { type: Number, required: true },
      vote: { type: String, enum: ['yes', 'no'], required: true },
    },
  ],
  startedAt: { type: Date, default: Date.now },
  endedAt: { type: Date },
  isActive: { type: Boolean, default: true },
});

export default mongoose.model<IVotingSession>('VotingSession', VotingSessionSchema);

