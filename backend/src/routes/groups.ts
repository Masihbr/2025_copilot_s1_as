import express from 'express';
import Group from '../models/Group';
import User from '../models/User';
import crypto from 'crypto';

const router = express.Router();

// Helper to generate a unique invite code
function generateInviteCode() {
  return crypto.randomBytes(3).toString('hex').toUpperCase();
}

// POST /groups - Create a group
router.post('/', async (req, res) => {
  const { name, ownerId } = req.body;
  if (!name || !ownerId) return res.status(400).json({ error: 'Missing name or ownerId' });
  try {
    const inviteCode = generateInviteCode();
    const group = new Group({
      name,
      owner: ownerId,
      members: [ownerId],
      inviteCode,
    });
    await group.save();
    res.json({ groupId: group._id, inviteCode });
  } catch (err) {
    res.status(500).json({ error: 'Failed to create group' });
  }
});

// POST /groups/join - Join a group by invite code
router.post('/join', async (req, res) => {
  const { inviteCode, userId } = req.body;
  if (!inviteCode || !userId) return res.status(400).json({ error: 'Missing inviteCode or userId' });
  try {
    const group = await Group.findOne({ inviteCode });
    if (!group) return res.status(404).json({ error: 'Group not found' });
    if (!group.members.includes(userId)) {
      group.members.push(userId);
      await group.save();
    }
    res.json({ groupId: group._id, inviteCode: group.inviteCode });
  } catch (err) {
    res.status(500).json({ error: 'Failed to join group' });
  }
});

export default router;

