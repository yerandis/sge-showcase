/**
 * Funciones utilitarias de formato.
 * Funciones puras: dado el mismo input, siempre devuelven el mismo output.
 * Sin efectos secundarios. Fáciles de testear.
 */

/**
 * Formatea un número como moneda.
 * Intl.NumberFormat es la API nativa del navegador para internacionalización.
 */
export function formatCurrency(amount: number): string {
  return new Intl.NumberFormat('es-ES', {
    style: 'currency',
    currency: 'EUR',
    minimumFractionDigits: 0,
    maximumFractionDigits: 0,
  }).format(amount);
}

/**
 * Formatea una fecha ISO a formato legible.
 * "2024-01-15" → "15 de enero de 2024"
 */
export function formatDate(dateString: string): string {
  if (!dateString) return '—';
  const date = new Date(dateString + 'T00:00:00'); // evita timezone offset
  return new Intl.DateTimeFormat('es-ES', {
    day: 'numeric',
    month: 'long',
    year: 'numeric',
  }).format(date);
}

/**
 * Formatea un datetime ISO a formato legible.
 */
export function formatDateTime(dateTimeString: string): string {
  if (!dateTimeString) return '—';
  return new Intl.DateTimeFormat('es-ES', {
    day: '2-digit',
    month: '2-digit',
    year: 'numeric',
    hour: '2-digit',
    minute: '2-digit',
  }).format(new Date(dateTimeString));
}

/**
 * Nombre completo del empleado.
 */
export function getFullName(firstName: string, lastName: string): string {
  return `${firstName} ${lastName}`;
}

/**
 * Iniciales del empleado para avatares.
 * "Ana García" → "AG"
 */
export function getInitials(firstName: string, lastName?: string): string {
  if (lastName) return `${firstName[0]}${lastName[0]}`.toUpperCase();
  return firstName.substring(0, 2).toUpperCase();
}

/**
 * Etiqueta legible del status.
 */
export function formatStatus(status: string): string {
  return status === 'ACTIVE' ? 'Activo' : 'Inactivo';
}

export function timeAgo(dateStr: string): string {
  const diff = Date.now() - new Date(dateStr).getTime();
  const mins  = Math.floor(diff / 60000);
  if (mins < 60)  return `hace ${mins} min`;
  const hrs = Math.floor(mins / 60);
  if (hrs < 24)   return `hace ${hrs}h`;
  const days = Math.floor(hrs / 24);
  if (days < 30)  return `hace ${days} día${days !== 1 ? 's' : ''}`;
  const months = Math.floor(days / 30);
  return `hace ${months} mes${months !== 1 ? 'es' : ''}`;
}