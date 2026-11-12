/**
 * Convierte la ruta del avatar del backend en una URL que el navegador
 * pueda cargar. Conserva las URL absolutas y resuelve las rutas relativas
 * contra el servidor de la API, no contra el servidor de Vite.
 */
export function resolveAvatarUrl(avatarUrl: string | null | undefined): string {
  if (!avatarUrl) return "";

  // Si el backend devuelve una URL completa, no debemos modificarla.
  if (/^https?:\/\//i.test(avatarUrl)) return avatarUrl;

  const apiBaseUrl = (
    import.meta.env.VITE_API_BASE_URL ?? "http://localhost:8080/api/v1"
  ).replace(/\/api\/v1\/?$/, "");

  // Admite rutas con o sin "/" inicial.
  return `${apiBaseUrl}/${avatarUrl.replace(/^\/+/, "")}`;
}
