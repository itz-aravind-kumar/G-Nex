import { useState, useRef } from 'react'
import { useMutation } from '@tanstack/react-query'
import { useAuth } from '@/contexts/AuthContext'
import { fileService } from '@/services/fileService'
import { X, Upload, CheckCircle, AlertCircle } from 'lucide-react'

interface UploadDialogProps {
  open: boolean
  onClose: () => void
  onUploadComplete: () => void
}

export default function UploadDialog({ open, onClose, onUploadComplete }: UploadDialogProps) {
  const { user } = useAuth()
  const [selectedFile, setSelectedFile] = useState<File | null>(null)
  const [progress, setProgress] = useState(0)
  const fileInputRef = useRef<HTMLInputElement>(null)

  const uploadMutation = useMutation({
    mutationFn: (file: File) => {
      if (!user) throw new Error('User not authenticated')
      return fileService.uploadFile(file, user.userId, setProgress)
    },
    onSuccess: () => {
      setTimeout(() => {
        onUploadComplete()
        resetForm()
      }, 1000)
    },
  })

  const resetForm = () => {
    setSelectedFile(null)
    setProgress(0)
    if (fileInputRef.current) {
      fileInputRef.current.value = ''
    }
  }

  const handleFileSelect = (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0]
    if (file) {
      setSelectedFile(file)
    }
  }

  const handleDrop = (e: React.DragEvent) => {
    e.preventDefault()
    const file = e.dataTransfer.files?.[0]
    if (file) {
      setSelectedFile(file)
    }
  }

  const handleUpload = () => {
    if (selectedFile) {
      uploadMutation.mutate(selectedFile)
    }
  }

  if (!open) return null

  return (
    <div className="fixed inset-0 bg-black/50 flex items-center justify-center z-50">
      <div className="bg-white rounded-xl shadow-2xl w-full max-w-md p-6">
        <div className="flex items-center justify-between mb-6">
          <h2 className="text-xl font-bold text-gray-900">Upload File</h2>
          <button
            onClick={() => {
              onClose()
              resetForm()
            }}
            className="p-1 hover:bg-gray-100 rounded transition"
          >
            <X className="w-5 h-5 text-gray-500" />
          </button>
        </div>

        {!uploadMutation.isPending && !uploadMutation.isSuccess ? (
          <div
            onDrop={handleDrop}
            onDragOver={e => e.preventDefault()}
            className="border-2 border-dashed border-gray-300 rounded-lg p-8 text-center hover:border-primary-400 transition"
          >
            <Upload className="w-12 h-12 text-gray-400 mx-auto mb-4" />
            <p className="text-gray-700 mb-2">
              {selectedFile ? selectedFile.name : 'Drag & drop a file here'}
            </p>
            <p className="text-sm text-gray-500 mb-4">or</p>
            <label className="inline-block px-4 py-2 bg-primary-600 text-white rounded-lg hover:bg-primary-700 cursor-pointer transition">
              Browse Files
              <input
                ref={fileInputRef}
                type="file"
                className="hidden"
                onChange={handleFileSelect}
              />
            </label>
          </div>
        ) : null}

        {uploadMutation.isPending && (
          <div className="space-y-4">
            <div className="flex items-center gap-3">
              <div className="flex-1">
                <p className="text-sm font-medium text-gray-900 mb-2">{selectedFile?.name}</p>
                <div className="h-2 bg-gray-200 rounded-full overflow-hidden">
                  <div
                    className="h-full bg-primary-600 transition-all duration-300"
                    style={{ width: `${progress}%` }}
                  ></div>
                </div>
                <p className="text-xs text-gray-500 mt-1">{progress}% uploaded</p>
              </div>
            </div>
          </div>
        )}

        {uploadMutation.isSuccess && (
          <div className="flex items-center gap-3 p-4 bg-green-50 rounded-lg">
            <CheckCircle className="w-6 h-6 text-green-600" />
            <div>
              <p className="font-medium text-green-900">Upload complete!</p>
              <p className="text-sm text-green-700">{selectedFile?.name}</p>
            </div>
          </div>
        )}

        {uploadMutation.isError && (
          <div className="flex items-center gap-3 p-4 bg-red-50 rounded-lg">
            <AlertCircle className="w-6 h-6 text-red-600" />
            <div>
              <p className="font-medium text-red-900">Upload failed</p>
              <p className="text-sm text-red-700">Please try again</p>
            </div>
          </div>
        )}

        <div className="flex gap-3 mt-6">
          <button
            onClick={() => {
              onClose()
              resetForm()
            }}
            className="flex-1 px-4 py-2 border border-gray-300 text-gray-700 rounded-lg hover:bg-gray-50 transition"
          >
            Cancel
          </button>
          {!uploadMutation.isPending && !uploadMutation.isSuccess && (
            <button
              onClick={handleUpload}
              disabled={!selectedFile}
              className="flex-1 px-4 py-2 bg-primary-600 text-white rounded-lg hover:bg-primary-700 transition disabled:opacity-50 disabled:cursor-not-allowed"
            >
              Upload
            </button>
          )}
        </div>
      </div>
    </div>
  )
}
