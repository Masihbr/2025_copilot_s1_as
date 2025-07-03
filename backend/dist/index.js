"use strict";
var __importDefault = (this && this.__importDefault) || function (mod) {
    return (mod && mod.__esModule) ? mod : { "default": mod };
};
Object.defineProperty(exports, "__esModule", { value: true });
const express_1 = __importDefault(require("express"));
const mongoose_1 = __importDefault(require("mongoose"));
const dotenv_1 = __importDefault(require("dotenv"));
const cors_1 = __importDefault(require("cors"));
const auth_1 = __importDefault(require("./routes/auth"));
const groups_1 = __importDefault(require("./routes/groups"));
const votingSessions_1 = __importDefault(require("./routes/votingSessions"));
// Load environment variables from .env file
dotenv_1.default.config();
const app = (0, express_1.default)();
const PORT = process.env.PORT || 4000;
app.use((0, cors_1.default)());
app.use(express_1.default.json());
// MongoDB connection
mongoose_1.default.connect(process.env.MONGODB_URI)
    .then(() => console.log('MongoDB connected'))
    .catch((err) => console.error('MongoDB connection error:', err));
// Health check endpoint
app.get('/', (req, res) => {
    res.send('MovieSwipe backend is running.');
});
app.use('/auth', auth_1.default);
app.use('/groups', groups_1.default);
app.use('/voting-sessions', votingSessions_1.default);
app.listen(PORT, () => {
    console.log(`Server running on port ${PORT}`);
});
