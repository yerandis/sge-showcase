// src/utils/errors.ts
export function getApiErrorMessage(err: unknown, fallback: string): string {
  if (err && typeof err === 'object' && 'response' in err) {
    const res = (err as { response?: { data?: { message?: string } } }).response;
    return res?.data?.message ?? fallback;
  }
  return fallback;
}