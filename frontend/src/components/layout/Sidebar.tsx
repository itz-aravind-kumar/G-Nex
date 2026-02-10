import { useLocation, useNavigate } from 'react-router-dom'
import { HardDrive, Share2, Clock, Trash2, Folder } from 'lucide-react'

const menuItems = [
  { icon: HardDrive, label: 'My Drive', path: '/', badge: null },
  { icon: Share2, label: 'Shared with me', path: '/shared', badge: null },
  { icon: Clock, label: 'Recent', path: '/recent', badge: null },
  { icon: Trash2, label: 'Trash', path: '/trash', badge: null },
]

export default function Sidebar() {
  const location = useLocation()
  const navigate = useNavigate()

  return (
    <aside className="w-60 bg-white border-r border-gray-200 p-4">
      <nav className="space-y-1">
        {menuItems.map(item => {
          const Icon = item.icon
          const isActive = location.pathname === item.path

          return (
            <button
              key={item.path}
              onClick={() => navigate(item.path)}
              className={`w-full flex items-center gap-3 px-4 py-2 rounded-lg transition ${
                isActive
                  ? 'bg-primary-50 text-primary-700 font-medium'
                  : 'text-gray-700 hover:bg-gray-100'
              }`}
            >
              <Icon className="w-5 h-5" />
              <span className="flex-1 text-left">{item.label}</span>
              {item.badge && (
                <span className="px-2 py-0.5 text-xs bg-gray-200 text-gray-700 rounded-full">
                  {item.badge}
                </span>
              )}
            </button>
          )
        })}
      </nav>

      <div className="mt-8">
        <h3 className="px-4 text-xs font-semibold text-gray-500 uppercase mb-2">Folders</h3>
        <div className="space-y-1">
          <button className="w-full flex items-center gap-3 px-4 py-2 text-gray-700 hover:bg-gray-100 rounded-lg transition">
            <Folder className="w-5 h-5 text-gray-400" />
            <span className="flex-1 text-left text-sm">Projects</span>
          </button>
          <button className="w-full flex items-center gap-3 px-4 py-2 text-gray-700 hover:bg-gray-100 rounded-lg transition">
            <Folder className="w-5 h-5 text-gray-400" />
            <span className="flex-1 text-left text-sm">Documents</span>
          </button>
        </div>
      </div>

      <div className="mt-8 px-4">
        <div className="p-4 bg-gray-50 rounded-lg">
          <p className="text-xs text-gray-600 mb-2">Storage</p>
          <div className="h-2 bg-gray-200 rounded-full overflow-hidden">
            <div className="h-full bg-primary-600 w-1/3"></div>
          </div>
          <p className="text-xs text-gray-500 mt-2">2.5 GB of 15 GB used</p>
        </div>
      </div>
    </aside>
  )
}
