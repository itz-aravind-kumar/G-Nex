import { useState } from 'react'
import { useQuery } from '@tanstack/react-query'
import { useAuth } from '@/contexts/AuthContext'
import { fileService } from '@/services/fileService'
import FileGrid from '@/components/files/FileGrid'
import UploadDialog from '@/components/files/UploadDialog'
import StorageStats from '@/components/stats/StorageStats'
import { Upload, FolderPlus, Grid3x3, List, Search, Clock } from 'lucide-react'

export default function Dashboard() {
  const { user } = useAuth()
  const [uploadOpen, setUploadOpen] = useState(false)
  const [viewMode, setViewMode] = useState<'grid' | 'list'>('grid')

  const { data: files, isLoading, refetch, error } = useQuery({
    queryKey: ['files', user?.userId],
    queryFn: async () => {
      console.log('Fetching files for userId:', user?.userId)
      const result = await fileService.listUserFiles(user!.userId)
      console.log('Files fetched:', result)
      return result
    },
    enabled: !!user,
  })

  // Log any errors for debugging
  if (error) {
    console.error('Failed to load files:', error)
  }

  const { data: stats } = useQuery({
    queryKey: ['storage-stats', user?.userId],
    queryFn: () => fileService.getUserStorageStats(user!.userId),
    enabled: !!user,
  })

  return (
    <div className="space-y-6 pb-8">
      {/* Header with Search */}
      <div className="bg-white border-b border-gray-200 -mx-8 -mt-6 px-8 py-4 mb-6">
        <div className="flex items-center justify-between gap-4">
          <div className="flex-1 max-w-2xl">
            <div className="relative">
              <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 w-5 h-5 text-gray-400" />
              <input
                type="text"
                placeholder="Search in Drive"
                className="w-full pl-10 pr-4 py-2.5 bg-gray-50 border border-gray-200 rounded-full focus:outline-none focus:bg-white focus:border-primary-500 transition"
              />
            </div>
          </div>
          <div className="flex items-center gap-2">
            <button
              onClick={() => setViewMode('grid')}
              className={`p-2 rounded-lg transition ${
                viewMode === 'grid' ? 'bg-primary-100 text-primary-700' : 'text-gray-600 hover:bg-gray-100'
              }`}
              title="Grid view"
            >
              <Grid3x3 className="w-5 h-5" />
            </button>
            <button
              onClick={() => setViewMode('list')}
              className={`p-2 rounded-lg transition ${
                viewMode === 'list' ? 'bg-primary-100 text-primary-700' : 'text-gray-600 hover:bg-gray-100'
              }`}
              title="List view"
            >
              <List className="w-5 h-5" />
            </button>
          </div>
        </div>
      </div>

      {/* Action Buttons */}
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-bold text-gray-900 flex items-center gap-2">
            <Clock className="w-6 h-6 text-gray-600" />
            My Drive
          </h1>
          <p className="text-sm text-gray-600 mt-1">
            {files?.length || 0} {files?.length === 1 ? 'file' : 'files'} · {stats ? `${(stats.totalSize / 1024 / 1024).toFixed(2)} MB` : '0 MB'} of 15 GB used
          </p>
        </div>
        <div className="flex gap-3">
          <button
            onClick={() => setUploadOpen(true)}
            className="flex items-center gap-2 px-5 py-2.5 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition shadow-sm font-medium"
          >
            <Upload className="w-4 h-4" />
            New
          </button>
        </div>
      </div>

      {/* Storage Stats */}
      {stats && <StorageStats stats={stats} />}

      {/* Quick Access */}
      {files && files.length > 0 && (
        <div>
          <h2 className="text-sm font-semibold text-gray-700 mb-3 uppercase tracking-wider">
            Suggested
          </h2>
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-3">
            {files.slice(0, 4).map((file) => (
              <div key={file.fileId} className="flex items-center gap-3 p-3 bg-white border border-gray-200 rounded-lg hover:bg-gray-50 hover:shadow-sm transition cursor-pointer">
                <div className="flex-shrink-0 w-10 h-10 bg-blue-50 rounded flex items-center justify-center">
                  <span className="text-blue-600 font-semibold text-xs">
                    {file.fileName.split('.').pop()?.toUpperCase().slice(0, 3)}
                  </span>
                </div>
                <div className="flex-1 min-w-0">
                  <p className="text-sm font-medium text-gray-900 truncate">{file.fileName}</p>
                  <p className="text-xs text-gray-500">Opened {new Date(file.uploadedAt).toLocaleDateString()}</p>
                </div>
              </div>
            ))}
          </div>
        </div>
      )}

      {/* Files Section */}
      <div>
        <h2 className="text-sm font-semibold text-gray-700 mb-3 uppercase tracking-wider">
          Files
        </h2>
        {isLoading ? (
          <div className="flex flex-col items-center justify-center py-16">
            <div className="animate-spin rounded-full h-12 w-12 border-4 border-blue-600 border-t-transparent"></div>
            <p className="mt-4 text-gray-600">Loading your files...</p>
          </div>
        ) : (
          <FileGrid files={files || []} onRefresh={refetch} viewMode={viewMode} />
        )}
      </div>

      <UploadDialog
        open={uploadOpen}
        onClose={() => setUploadOpen(false)}
        onUploadComplete={() => {
          setUploadOpen(false)
          refetch()
        }}
      />
    </div>
  )
}
