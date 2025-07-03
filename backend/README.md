# MovieSwipe Backend (Node.js + TypeScript)

This is the backend for the MovieSwipe app. It provides REST APIs for group management, user authentication, voting sessions, and movie recommendations.

## Tech Stack
- Node.js
- TypeScript
- Express.js
- MongoDB (self-hosted, not Atlas/Realm)
- TMDB API (for movie data)
- Google OAuth (for authentication)
- Firebase Cloud Messaging (for push notifications)

## Setup Instructions

### 1. Prerequisites
- Node.js >= 18.x
- npm >= 9.x
- MongoDB (self-hosted, not Atlas/Realm)
- TMDB API Key (https://www.themoviedb.org/documentation/api)
- Google OAuth credentials (https://console.developers.google.com)
- Firebase Cloud Messaging credentials (https://console.firebase.google.com)

### 2. Clone the Repository
```
git clone <your-repo-url>
cd backend
```

### 3. Install Dependencies
```
npm install
```

### 4. Environment Variables
Create a `.env` file in the backend root with the following:
```
PORT=4000
MONGODB_URI=mongodb://localhost:27017/movieswipe
TMDB_API_KEY=your_tmdb_api_key
GOOGLE_CLIENT_ID=your_google_client_id
GOOGLE_CLIENT_SECRET=your_google_client_secret
FCM_SERVER_KEY=your_fcm_server_key
```

### 5. Run the Server
```
npm run dev
```

### 6. Deploy to Azure
- Use Azure App Service or a Virtual Machine.
- Set environment variables in Azure portal.
- Ensure MongoDB is accessible from Azure.

---

## Folder Structure
- `src/` - Source code
- `src/models/` - Mongoose models
- `src/routes/` - Express routes
- `src/controllers/` - Route handlers
- `src/services/` - External API integrations
- `src/utils/` - Utility functions

---

## API Overview
- `/auth/google` - Google OAuth login
- `/groups` - Group management
- `/groups/:id/invite` - Invite via code
- `/groups/:id/join` - Join group
- `/groups/:id/vote-session` - Start/end voting
- `/groups/:id/vote` - Submit vote
- `/groups/:id/result` - Get movie result

---

## Contact
For questions, contact the project maintainer.

