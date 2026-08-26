// src/features/packages/components/PackageStatusBadge.jsx
const statusConfig = {
  CREATED: { label: 'Created', color: 'bg-gray-100 text-gray-700' },
  PROCESSING: { label: 'Processing', color: 'bg-blue-100 text-blue-700' },
  READY_FOR_PICKUP: { label: 'Ready for Pickup', color: 'bg-yellow-100 text-yellow-700' },
  IN_TRANSIT: { label: 'In Transit', color: 'bg-purple-100 text-purple-700' },
  DELIVERED: { label: 'Delivered', color: 'bg-green-100 text-green-700' },
  ON_HOLD: { label: 'On Hold', color: 'bg-orange-100 text-orange-700' },
  CANCELLED: { label: 'Cancelled', color: 'bg-red-100 text-red-700' },
};

export default function PackageStatusBadge({ status }) {
  const config = statusConfig[status] || statusConfig.CREATED;
  
  return (
    <span className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium ${config.color}`}>
      <span className="w-1.5 h-1.5 rounded-full mr-1.5 bg-current opacity-60" />
      {config.label}
    </span>
  );
}