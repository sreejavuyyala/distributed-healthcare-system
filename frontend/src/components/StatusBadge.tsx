import type { FeedStatusValue } from '../types';

const CONFIG: Record<FeedStatusValue, { label: string; className: string }> = {
  SUCCESS: { label: 'Success', className: 'badge-success' },
  FAILED: { label: 'Failed', className: 'badge-danger' },
  RUNNING: { label: 'Running', className: 'badge-warning' },
};

export function StatusBadge({ status }: { status: FeedStatusValue }) {
  const cfg = CONFIG[status] ?? { label: status, className: 'badge-warning' };
  return (
    <span className={`badge ${cfg.className}`}>
      <span className="dot" />
      {cfg.label}
    </span>
  );
}