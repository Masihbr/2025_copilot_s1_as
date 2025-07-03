// User model for MongoDB
import mongoose, { Schema, Document } from 'mongoose';

export interface IUser extends Document {
  googleId: string;
  name: string;
  email: string;
  avatarUrl?: string;
  fcmToken?: string;
  genrePreferences: string[];
}

const UserSchema: Schema = new Schema({
  googleId: { type: String, required: true, unique: true },
  name: { type: String, required: true },
  email: { type: String, required: true },
  avatarUrl: { type: String },
  fcmToken: { type: String },
  genrePreferences: { type: [String], default: [] },
});

export default mongoose.model<IUser>('User', UserSchema);

