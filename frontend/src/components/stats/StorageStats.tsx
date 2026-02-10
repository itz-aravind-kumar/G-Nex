import { HardDrive, TrendingUp } from 'lucide-react'

interface StorageStatsProps {
  stats: {
    totalFiles: number
    totalSize: number
    usedSize: number
    quotaSize: number
  }
}

export default function StorageStats({ stats }: StorageStatsProps) {
  const usagePercent = (stats.usedSize / stats.quotaSize) * 100
  const usedGB = (stats.usedSize / 1024 / 1024 / 1024).toFixed(2)
  const quotaGB = (stats.quotaSize / 1024 / 1024 / 1024).toFixed(0)

  return (
    <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
      <div className="bg-white rounded-lg border border-gray-200 p-6">
        <div className="flex items-center gap-3 mb-4">
          <div className="p-2 bg-primary-50 rounded-lg">
            <HardDrive className="w-6 h-6 text-primary-600" />
          </div>
          <div>
            <p className="text-sm text-gray-600">Storage Used</p>
            <p className="text-2xl font-bold text-gray-900">
              {usedGB} GB <span className="text-sm text-gray-500">/ {quotaGB} GB</span>
            </p>
          </div>
        </div>
        <div className="h-2 bg-gray-200 rounded-full overflow-hidden">
          <div
            className={`h-full transition-all ${usagePercent > 80 ? 'bg-red-600' : 'bg-primary-600'}`}
            style={{ width: `${Math.min(usagePercent, 100)}%` }}
          ></div>
        </div>
      </div>

      <div className="bg-white rounded-lg border border-gray-200 p-6">
        <div className="flex items-center gap-3">
          <div className="p-2 bg-green-50 rounded-lg">
            <TrendingUp className="w-6 h-6 text-green-600" />
          </div>
          <div>
            <p className="text-sm text-gray-600">Total Files</p>
            <p className="text-2xl font-bold text-gray-900">{stats.totalFiles}</p>
          </div>
        </div>
      </div>

      <div className="bg-white rounded-lg border border-gray-200 p-6">
        <div className="flex items-center gap-3">
          <div className="p-2 bg-blue-50 rounded-lg">
            <HardDrive className="w-6 h-6 text-blue-600" />
          </div>
          <div>
            <p className="text-sm text-gray-600">Available</p>
            <p className="text-2xl font-bold text-gray-900">
              {(quotaGB - parseFloat(usedGB)).toFixed(2)} GB
            </p>
          </div>
        </div>
      </div>
    </div>
  )
}
