# G-Nex Drive - Frontend

Production-grade React TypeScript frontend for G-Nex Drive cloud storage platform.

## Tech Stack

- **Vite 5.1.0** - Lightning-fast build tool with HMR
- **React 18.2.0** - UI library with hooks
- **TypeScript 5.3.3** - Type-safe development
- **TailwindCSS 3.4.1** - Utility-first CSS framework
- **React Router 6.22.0** - Client-side routing
- **TanStack Query 5.20.0** - Server state management & caching
- **Zustand 4.5.0** - Lightweight global state management
- **Axios 1.6.7** - HTTP client with interceptors
- **Lucide React** - Beautiful icon library

## Project Structure

```
frontend/
├── src/
│   ├── components/
│   │   ├── files/         # File grid, upload dialog
│   │   ├── layout/        # Navbar, sidebar, main layout
│   │   └── stats/         # Storage statistics
│   ├── contexts/          # React contexts (Auth)
│   ├── pages/             # Page components (Login, Dashboard)
│   ├── services/          # API services (auth, files)
│   ├── types/             # TypeScript interfaces
│   ├── App.tsx            # Root component with routing
│   ├── main.tsx           # Application entry point
│   └── index.css          # Global styles
├── public/                # Static assets
├── .env                   # Environment variables
└── package.json           # Dependencies and scripts
```

## Features

✅ **Authentication** - JWT token-based auth with localStorage persistence  
✅ **File Upload** - Drag & drop with progress tracking  
✅ **File Download** - Direct download from MinIO  
✅ **File Management** - View, delete files with metadata  
✅ **Storage Stats** - Real-time storage usage dashboard  
✅ **Protected Routes** - Auth-guarded pages  
✅ **Responsive Design** - Mobile-friendly UI with TailwindCSS  
✅ **API Proxy** - Vite proxy forwards /api to backend (port 8080)  

## Getting Started

### Prerequisites

- Node.js 18+ and npm
- Backend services running (API Gateway on port 8080)

### Installation

```bash
cd frontend
npm install
```

### Environment Variables

Copy `.env.example` to `.env` and configure:

```env
VITE_API_BASE_URL=http://localhost:8080
VITE_APP_NAME=G-Nex Drive
```

### Development

```bash
npm run dev
```

Opens at http://localhost:3000 with hot module replacement.

### Build for Production

```bash
npm run build
```

Outputs optimized bundle to `dist/` directory.

### Preview Production Build

```bash
npm run preview
```

### Code Quality

```bash
npm run lint      # Run ESLint
npm run format    # Format with Prettier
```

## API Integration

The frontend communicates with backend services through the API Gateway:

- **Authentication**: `POST /api/v1/auth/generate-token`
- **File Upload**: `POST /api/v1/files/upload`
- **File Download**: `GET /api/v1/files/:fileId/download`
- **File Delete**: `DELETE /api/v1/files/:fileId`
- **List Files**: `GET /api/v1/metadata/user/:userId`
- **Storage Stats**: `GET /api/v1/metadata/user/:userId/storage-stats`

All requests include `Authorization: Bearer <token>` header (auto-injected by axios interceptor).

## Usage

1. **Login**: Enter userId, username, email to generate JWT token
2. **Upload**: Click "Upload File" button or drag & drop
3. **Browse**: View files in grid with thumbnails, size, date
4. **Download**: Click download icon on any file
5. **Delete**: Click trash icon (with confirmation)

## Architecture

- **API Client** (`services/apiClient.ts`): Axios instance with request/response interceptors for JWT injection and 401 handling
- **Auth Context** (`contexts/AuthContext.tsx`): Global auth state with login/logout methods
- **TanStack Query**: Handles server state caching, automatic refetching, loading states
- **Protected Routes**: Redirect to /login if not authenticated
- **Type Safety**: All API responses typed with TypeScript interfaces

## Development Notes

- Vite proxy forwards `/api` requests to `http://localhost:8080` (API Gateway)
- Path alias `@` maps to `./src` for clean imports
- JWT tokens stored in localStorage with automatic injection
- 401/403 responses trigger automatic logout and redirect to login
- TailwindCSS custom theme with primary blue color palette

## Troubleshooting

**Cannot connect to backend**: Ensure API Gateway is running on port 8080  
**CORS errors**: Proxy should handle this, check vite.config.ts  
**Token expired**: Tokens expire after 1 hour, re-login to generate new token  
**Build errors**: Run `npm install` to ensure all dependencies are installed

## Future Enhancements

- Folder hierarchy support
- File sharing & permissions
- Search functionality
- Trash & file recovery
- File versioning UI
- Activity logs
- Real-time notifications
