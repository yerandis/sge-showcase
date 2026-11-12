interface IconProps {
  d: string;
  size?: string;
  className?: string;
}

export default function Icon({ d, size = '1.125rem', className }: IconProps) {
  return (
    <svg
      style={{ width: size, height: size, flexShrink: 0 }}
      className={className}
      fill="none"
      viewBox="0 0 24 24"
      stroke="currentColor"
      strokeWidth={2}
    >
      <path strokeLinecap="round" strokeLinejoin="round" d={d} />
    </svg>
  );
}