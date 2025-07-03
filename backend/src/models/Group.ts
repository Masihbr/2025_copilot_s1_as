import mongoose, { Schema, Document, Types } from 'mongoose';

export interface IGroup extends Document {
  name: string;
  owner: Types.ObjectId; // User _id
  members: Types.ObjectId[]; // User _id[]
  inviteCode: string;
  createdAt: Date;
}

const GroupSchema: Schema = new Schema({
  name: { type: String, required: true },
  owner: { type: Schema.Types.ObjectId, ref: 'User', required: true },
  members: [{ type: Schema.Types.ObjectId, ref: 'User' }],
  inviteCode: { type: String, required: true, unique: true },
  createdAt: { type: Date, default: Date.now },
});

export default mongoose.model<IGroup>('Group', GroupSchema);

