import { useState, useEffect } from 'react'
import { FileMetadata } from '@/types'
import { fileService } from '@/services/fileService'
import ShareDialog from '../sharing/ShareDialog'
import {
  File,
  FileText,
  Image,
  Film,
  Music,
  Archive,
  Download,
  Trash2,
  Share2,
  MoreVertical,
  Loader2,
} from 'lucide-react'

interface FileGridProps {
  files: FileMetadata[]
  onRefresh: () => void
  viewMode?: 'grid' | 'list'
}

const getFileIcon = (contentType: string) => {
  if (contentType.startsWith('image/')) return Image
  if (contentType.startsWith('video/')) return Film
  if (contentType.startsWith('audio/')) return Music
  if (contentType.startsWith('text/')) return FileText
  if (contentType.includes('zip') || contentType.includes('rar')) return Archive
  return File
}

const formatFileSize = (bytes: number) => {
  if (bytes === 0) return '0 Bytes'
  const k = 1024
  const sizes = ['Bytes', 'KB', 'MB', 'GB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return Math.round(bytes / Math.pow(k, i) * 100) / 100 + ' ' + sizes[i]
}

const formatDate = (dateString: string) => {
  const date = new Date(dateString)
  return new Intl.DateTimeFormat('en-US', {
    month: 'short',
    day: 'numeric',
    year: 'numeric',
  }).format(date)
}

export default function FileGrid({ files, onRefresh, viewMode = 'grid' }: FileGridProps) {
  const [selectedFile, setSelectedFile] = useState<string | null>(null)
  const [downloading, setDownloading] = useState<string | null>(null)
  const [thumbnails, setThumbnails] = useState<Record<string, string | null>>({})
  const [loadingThumbnails, setLoadingThumbnails] = useState<Record<string, boolean>>({})
  const [shareDialogOpen, setShareDialogOpen] = useState(false)
  const [fileToShare, setFileToShare] = useState<FileMetadata | null>(null)

  // Fetch thumbnails for supported file types
  useEffect(() => {
    const fetchThumbnails = async () => {
      for (const file of files) {
        // Only fetch thumbnails for images and PDFs
        if (file.contentType.startsWith('image/') || file.contentType === 'application/pdf') {
          // Skip if already loaded or loading
          if (thumbnails[file.fileId] !== undefined || loadingThumbnails[file.fileId]) continue

          setLoadingThumbnails(prev => ({ ...prev, [file.fileId]: true }))
          
          try {
            const thumbnailUrl = await fileService.getThumbnail(file.fileId, 'GRID')
            setThumbnails(prev => ({ ...prev, [file.fileId]: thumbnailUrl }))
          } catch (error) {
            console.error('Failed to fetch thumbnail:', error)
            setThumbnails(prev => ({ ...prev, [file.fileId]: null }))
          } finally {
            setLoadingThumbnails(prev => ({ ...prev, [file.fileId]: false }))
          }
        }
      }
    }

    fetchThumbnails()
  }, [files])

  const handleDownload = async (file: FileMetadata) => {
    try {
      setDownloading(file.fileId)
      const blob = await fileService.downloadFile(file.fileId)
      const url = window.URL.createObjectURL(blob)
      const a = document.createElement('a')
      a.href = url
      a.download = file.fileName
      document.body.appendChild(a)
      a.click()
      window.URL.revokeObjectURL(url)
      document.body.removeChild(a)
    } catch (error) {
      console.error('Download failed:', error)
      alert('Failed to download file. Please try again.')
    } finally {
      setDownloading(null)
    }
  }

  const handleDelete = async (fileId: string) => {
    if (!confirm('Are you sure you want to delete this file? This action cannot be undone.')) return

    try {
      await fileService.deleteFile(fileId)
      onRefresh()
    } catch (error) {
      console.error('Delete failed:', error)
      alert('Failed to delete file. Please try again.')
    }
  }

  const handleShare = (file: FileMetadata) => {
    setFileToShare(file)
    setShareDialogOpen(true)
  }

  // Ensure files is always an array
  if (!Array.isArray(files) || files.length === 0) {
    return (
      <div className="flex flex-col items-center justify-center py-24 text-center bg-white rounded-lg border-2 border-dashed border-gray-300">
        <div className="w-24 h-24 bg-gray-100 rounded-full flex items-center justify-center mb-6">
          <File className="w-12 h-12 text-gray-400" />
        </div>
        <h3 className="text-xl font-semibold text-gray-900 mb-2">No files in My Drive</h3>
        <p className="text-gray-600 mb-6 max-w-sm">
          Upload files to get started. Drag and drop files here or click the New button
        </p>
        <div className="flex gap-3">
          <button className="px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition font-medium">
            Upload files
          </button>
          <button className="px-4 py-2 border border-gray-300 text-gray-700 rounded-lg hover:bg-gray-50 transition font-medium">
            Learn more
          </button>
        </div>
      </div>
    )
  }

  if (viewMode === 'list') {
    return (
      <div className="bg-white rounded-lg border border-gray-200 overflow-hidden">
        <table className="w-full">
          <thead className="bg-gray-50 border-b border-gray-200">
            <tr>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-600 uppercase tracking-wider">Name</th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-600 uppercase tracking-wider">Owner</th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-600 uppercase tracking-wider">Last modified</th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-600 uppercase tracking-wider">File size</th>
              <th className="px-6 py-3 text-right text-xs font-medium text-gray-600 uppercase tracking-wider">Actions</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-gray-200">
            {files.map((file) => {
              const Icon = getFileIcon(file.contentType)
              return (
                <tr key={file.fileId} className="hover:bg-gray-50 transition">
                  <td className="px-6 py-4">
                    <div className="flex items-center gap-3">
                      <div className="p-2 bg-blue-50 rounded">
                        <Icon className="w-5 h-5 text-blue-600" />
                      </div>
                      <span className="font-medium text-gray-900">{file.fileName}</span>
                    </div>
                  </td>
                  <td className="px-6 py-4 text-sm text-gray-600">me</td>
                  <td className="px-6 py-4 text-sm text-gray-600">{formatDate(file.uploadedAt)}</td>
                  <td className="px-6 py-4 text-sm text-gray-600">{formatFileSize(file.fileSize)}</td>
                  <td className="px-6 py-4">
                    <div className="flex items-center justify-end gap-1">
                      <button
                        onClick={() => handleDownload(file)}
                        disabled={downloading === file.fileId}
                        className="p-2 text-gray-600 hover:bg-gray-100 rounded transition"
                        title="Download"
                      >
                        <Download className="w-4 h-4" />
                      </button>
                      <button
                        onClick={() => handleShare(file)}
                        className="p-2 text-gray-600 hover:bg-gray-100 rounded transition"
                        title="Share"
                      >
                        <Share2 className="w-4 h-4" />
                      </button>
                      <button
                        onClick={() => handleDelete(file.fileId)}
                        className="p-2 text-red-600 hover:bg-red-50 rounded transition"
                        title="Delete"
                      >
                        <Trash2 className="w-4 h-4" />
                      </button>
                    </div>
                  </td>
                </tr>
              )
            })}
          </tbody>
        </table>
      </div>
    )
  }

  return (
    <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-5 gap-4">
      {files.map(file => {
        const Icon = getFileIcon(file.contentType)
        const isSelected = selectedFile === file.fileId

        return (
          <div
            key={file.fileId}
            onClick={() => setSelectedFile(file.fileId)}
            className={`group bg-white rounded-lg border transition cursor-pointer hover:shadow-md ${
              isSelected ? 'border-blue-500 shadow-md ring-1 ring-blue-500' : 'border-gray-200 hover:border-blue-300'
            }`}
          >
            {/* File Preview */}
            <div className="aspect-square bg-gradient-to-br from-gray-50 to-gray-100 rounded-t-lg flex items-center justify-center relative overflow-hidden">
              {loadingThumbnails[file.fileId] ? (
                <Loader2 className="w-12 h-12 text-gray-400 animate-spin" />
              ) : thumbnails[file.fileId] ? (
                <img 
                  src={thumbnails[file.fileId]!} 
                  alt={file.fileName}
                  className="absolute inset-0 w-full h-full object-cover"
                  onError={(e) => {
                    // Fallback to icon if image fails to load
                    e.currentTarget.style.display = 'none'
                    setThumbnails(prev => ({ ...prev, [file.fileId]: null }))
                  }}
                />
              ) : file.contentType.startsWith('image/') || file.contentType === 'application/pdf' ? (
                <>
                  <div className="absolute inset-0 bg-gradient-to-br from-blue-50 via-indigo-50 to-purple-50 opacity-70"></div>
                  <Icon className="w-16 h-16 text-gray-400 relative z-10" />
                  <div className="absolute bottom-2 left-2 right-2 text-center">
                    <span className="text-xs text-gray-500 bg-white/80 px-2 py-1 rounded">Generating thumbnail...</span>
                  </div>
                </>
              ) : (
                <>
                  <div className="absolute inset-0 bg-gradient-to-br from-blue-50 to-gray-50 opacity-50"></div>
                  <Icon className="w-16 h-16 text-gray-400 relative z-10" />
                </>
              )}
              {/* Hover actions */}
              <div className="absolute top-2 right-2 opacity-0 group-hover:opacity-100 transition-opacity">
                <button
                  onClick={(e) => {
                    e.stopPropagation()
                    handleDelete(file.fileId)
                  }}
                  className="p-2 bg-white rounded-full shadow-lg hover:bg-red-50 transition"
                >
                  <Trash2 className="w-4 h-4 text-red-600" />
                </button>
              </div>
            </div>

            {/* File Info */}
            <div className="p-4">
              <h3 className="font-medium text-gray-900 mb-1 truncate text-sm" title={file.fileName}>
                {file.fileName}
              </h3>
              <div className="flex items-center justify-between text-xs text-gray-500">
                <span>{formatFileSize(file.fileSize)}</span>
                <span>{formatDate(file.uploadedAt)}</span>
              </div>

              {/* Actions */}
              <div className="mt-3 flex items-center gap-2">
                <button
                  onClick={e => {
                    e.stopPropagation()
                    handleDownload(file)
                  }}
                  disabled={downloading === file.fileId}
                  className="flex-1 flex items-center justify-center gap-1.5 px-3 py-2 text-sm bg-blue-50 text-blue-700 rounded-lg hover:bg-blue-100 transition disabled:opacity-50 font-medium"
                >
                  <Download className="w-4 h-4" />
                  {downloading === file.fileId ? 'Downloading...' : 'Download'}
                </button>
                <button
                  onClick={e => {
                    e.stopPropagation()
                    handleShare(file)
                  }}
                  className="p-2 text-gray-600 hover:bg-gray-100 rounded-lg transition"
                  title="Share"
                >
                  <Share2 className="w-4 h-4" />
                </button>
              </div>
            </div>
          </div>
        )
      })}

      {/* Share Dialog */}
      {fileToShare && (
        <ShareDialog
          isOpen={shareDialogOpen}
          onClose={() => {
            setShareDialogOpen(false)
            setFileToShare(null)
          }}
          resourceId={fileToShare.fileId}
          resourceType="FILE"
          resourceName={fileToShare.fileName}
        />
      )}
    </div>
  )
}
