import { useState, type FormEvent } from "react";
import { useAuth } from "../../context/AuthContext";
import {
  changePassword,
  uploadProfileAvatar,
} from "../../services/authService";
import { useBreadcrumb } from "../../hooks/useBreadcrumb";
import Breadcrumb from "../../components/ui/Breadcrumb/Breadcrumb";
import styles from "./MyProfilePage.module.css";
import { Link } from "react-router-dom";
import { resolveAvatarUrl } from "../../utils/avatar";

export default function MyProfilePage() {
  const MAX_AVATAR_SIZE = 2 * 1024 * 1024;
  // const { user } = useAuth();
  const { user, updateUser } = useAuth();

  // updateUser actualiza el contexto y localStorage para que el sidebar
  // refleje la nueva foto sin tener que cerrar sesión.

  const [currentPassword, setCurrentPassword] = useState("");
  const [newPassword, setNewPassword] = useState("");
  const [confirmPassword, setConfirmPassword] = useState("");

  const [selectedAvatar, setSelectedAvatar] = useState<File | null>(null);
  const [avatarLoading, setAvatarLoading] = useState(false);

  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");
  const [success, setSuccess] = useState("");

  const breadcrumb = useBreadcrumb(user ? `${user.username}` : undefined);

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();

    setError("");
    setSuccess("");

    if (newPassword !== confirmPassword) {
      setError("La nueva contraseña y su confirmación no coinciden.");
      return;
    }

    if (newPassword.length < 8 || newPassword.length > 72) {
      setError("La nueva contraseña debe tener entre 8 y 72 caracteres.");
      return;
    }

    setLoading(true);

    try {
      await changePassword({
        currentPassword,
        newPassword,
      });

      setCurrentPassword("");
      setNewPassword("");
      setConfirmPassword("");
      setSuccess("Tu contraseña se actualizó correctamente.");
    } catch {
      setError(
        "No se pudo actualizar la contraseña. Comprueba la contraseña actual e inténtalo de nuevo.",
      );
    } finally {
      setLoading(false);
    }
  }

  async function handleAvatarUpload() {
    if (!selectedAvatar) return;

    setAvatarLoading(true);
    setError("");
    setSuccess("");

    try {
      const avatarUrl = await uploadProfileAvatar(selectedAvatar);

      // Actualiza el contexto y la sesión local para reflejar la foto
      // tanto en esta página como en el sidebar.
      updateUser({ avatarUrl });

      setSelectedAvatar(null);
      setSuccess("Tu foto de perfil se actualizó correctamente.");
    } catch (err) {
      // Conservamos el mensaje real en la consola para identificar
      // si el fallo viene de Axios, de la autenticación o del backend.
      console.error("Error al subir la foto de perfil:", err);

      setError(
        "No se pudo subir la foto. Comprueba el archivo e inténtalo de nuevo.",
      );
    } finally {
      setAvatarLoading(false);
    }
  }

  if (!user) {
    return <p>No se pudo cargar la información del usuario.</p>;
  }

  return (
    <div className={styles.page}>
      <Breadcrumb items={breadcrumb} />

      <div className={styles.pageHeader}>
        <Link to="/dashboard" className={styles.backBtn}>
          <svg
            fill="none"
            viewBox="0 0 24 24"
            stroke="currentColor"
            strokeWidth={2}
          >
            <path
              strokeLinecap="round"
              strokeLinejoin="round"
              d="M15 19l-7-7 7-7"
            />
          </svg>
        </Link>

        <div className={styles.headerInfo}>
          <h1 className={styles.pageTitle}>Mi perfil</h1>
          <p className={styles.pageSubtitle}>
            Administra tu información personal y la seguridad de tu cuenta.
          </p>
        </div>
      </div>

      {/* Cabecera visual del perfil */}

      <section className={styles.profileHero}>
        <div className={styles.avatarColumn}>
          <div className={styles.avatar}>
            {user.avatarUrl ? (
              <img
                src={resolveAvatarUrl(user.avatarUrl)}
                alt={`Foto de ${user.username}`}
              />
            ) : (
              // Iniciales de respaldo mientras el usuario no tenga foto.
              `${user.username.substring(0, 2).toUpperCase()}`
            )}
          </div>

          <div className={styles.avatarActions}>
            <label className={styles.secondaryButton}>
              Seleccionar foto
              <input
                className={styles.fileInput}
                type="file"
                accept="image/png,image/jpeg,image/webp"
                onChange={(event) => {
                  const file = event.target.files?.[0] ?? null;

                  setError("");
                  setSuccess("");

                  if (file && file.size > MAX_AVATAR_SIZE) {
                    setSelectedAvatar(null);
                    setError(
                      "La imagen supera los 2 MB permitidos. Selecciona una imagen más pequeña.",
                    );
                    event.target.value = "";
                    return;
                  }

                  setSelectedAvatar(file);
                }}
              />
            </label>

            <button
              type="button"
              className={styles.secondaryButton}
              disabled={!selectedAvatar || avatarLoading}
              onClick={handleAvatarUpload}
            >
              {avatarLoading ? "Subiendo foto…" : "Actualizar foto"}
            </button>
          </div>
        </div>

        <div className={styles.heroContent}>
          <span className={styles.eyebrow}>CUENTA PERSONAL</span>
          <h2 className={styles.heroName}>{user.fullName || user.username}</h2>
          <p className={styles.heroUsername}>@{user.username}</p>

          <div className={styles.heroBadges}>
            <span className={styles.badge}>
              <span className={styles.badgeDot} />
              Cuenta activa
            </span>
          </div>
        </div>
      </section>

      {/* Dos tarjetas: datos personales y seguridad */}
      <div className={styles.profileGrid}>
        <section className={styles.card}>
          <div className={styles.cardHeader}>
            <div>
              <span className={styles.cardEyebrow}>PERFIL</span>
              <h2 className={styles.cardTitle}>Mis datos</h2>
              <p className={styles.cardDescription}>
                Información asociada a tu cuenta.
              </p>
            </div>
          </div>

          <div className={styles.cardBody}>
            <dl className={styles.dataList}>
              <div className={styles.dataItem}>
                <dt className={styles.dataLabel}>Nombre completo</dt>
                <dd className={styles.dataValue}>
                  {user.fullName || "No disponible"}
                </dd>
              </div>
              <div className={styles.dataItem}>
                <dt className={styles.dataLabel}>Usuario</dt>
                <dd className={styles.dataValue}>{user.username}</dd>
              </div>
              <div className={styles.dataItem}>
                <dt className={styles.dataLabel}>Correo electrónico</dt>
                <dd className={styles.dataValue}>
                  {user.email || "No disponible"}
                </dd>
              </div>
              <div className={styles.dataItem}>
                <dt className={styles.dataLabel}>Roles</dt>
                <dd className={styles.roleList}>
                  {user.roles.length > 0
                    ? user.roles.map((role) => (
                        <span className={styles.roleChip} key={role}>
                          {role}
                        </span>
                      ))
                    : "Sin roles asignados"}
                </dd>
              </div>
            </dl>
          </div>
        </section>

        <section className={styles.card}>
          <div className={styles.cardHeader}>
            <div>
              <span className={styles.cardEyebrow}>SEGURIDAD</span>
              <h2 className={styles.cardTitle}>Cambiar contraseña</h2>
              <p className={styles.cardDescription}>
                Confirma tu contraseña actual antes de guardar una nueva.
              </p>
            </div>
          </div>

          <div className={styles.cardBody}>
            <form className={styles.form} onSubmit={handleSubmit}>
              <div className={styles.field}>
                <label className={styles.label} htmlFor="currentPassword">
                  Contraseña actual
                </label>
                <input
                  className={styles.input}
                  id="currentPassword"
                  type="password"
                  autoComplete="current-password"
                  value={currentPassword}
                  onChange={(event) => setCurrentPassword(event.target.value)}
                  required
                />
              </div>

              <div className={styles.field}>
                <label className={styles.label} htmlFor="newPassword">
                  Nueva contraseña
                </label>
                <input
                  className={styles.input}
                  id="newPassword"
                  type="password"
                  autoComplete="new-password"
                  minLength={8}
                  maxLength={72}
                  value={newPassword}
                  onChange={(event) => setNewPassword(event.target.value)}
                  required
                />
                <p className={styles.hint}>Entre 8 y 72 caracteres.</p>
              </div>

              <div className={styles.field}>
                <label className={styles.label} htmlFor="confirmPassword">
                  Confirmar nueva contraseña
                </label>
                <input
                  className={styles.input}
                  id="confirmPassword"
                  type="password"
                  autoComplete="new-password"
                  minLength={8}
                  maxLength={72}
                  value={confirmPassword}
                  onChange={(event) => setConfirmPassword(event.target.value)}
                  required
                />
              </div>

              {error && (
                <p
                  className={`${styles.feedback} ${styles.feedbackError}`}
                  role="alert"
                >
                  {error}
                </p>
              )}
              {success && (
                <p
                  className={`${styles.feedback} ${styles.feedbackSuccess}`}
                  role="status"
                >
                  {success}
                </p>
              )}

              <div className={styles.formFooter}>
                <button
                  className={styles.primaryButton}
                  type="submit"
                  disabled={loading}
                >
                  {loading ? "Actualizando…" : "Actualizar contraseña"}
                </button>
              </div>
            </form>
          </div>
        </section>
      </div>
    </div>
  );
}
