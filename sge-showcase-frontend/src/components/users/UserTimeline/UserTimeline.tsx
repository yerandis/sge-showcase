import { useState, useEffect } from "react";
import styles from "../../employees/EmployeeForm/EmployeeTimeline/EmployeeTimeline.module.css";
import {
  getUserHistory,
  type UserHistoryEntry,
} from "../../../services/userServices";
import { timeAgo } from "../../../utils/formatters";
import Spinner from "../../ui/Spinner/Spinner";

const FIELD_LABELS: Record<string, string> = {
  username: "Nombre Usuario",
  password: "Contraseña",
  email: "Correo",
  firstName: "Nombre",
  lastName: "Apellido",
  avatarUrl: "Avatar",
  roles: "Roles",
  employee: "Empleado",
  active: "Estado",
};

export default function UserTimeline({ userId }: { userId: string }) {
  const [history, setHistory] = useState<UserHistoryEntry[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    getUserHistory(userId)
      .then(setHistory)
      .catch(() => setError("No se pudo cargar el historial de cambios."))
      .finally(() => setIsLoading(false));
  }, [userId]);

  if (isLoading) return <Spinner message="Cargando historial..." />;
  if (error) return <p style={{ color: "var(--color-danger)" }}>{error}</p>;
  if (history.length === 0)
    return (
      <p style={{ color: "var(--color-text-secondary)" }}>
        Sin historial de cambios.
      </p>
    );

  return (
    <div className={styles.timeline}>
      {history.map((entry, index) => (
        <div key={entry.id} className={styles.entry}>
          {/* Línea vertical */}
          <div className={styles.lineWrapper}>
            <div
              className={`${styles.dot} ${entry.action === "CREATE" ? styles.dotCreate : styles.dotUpdate}`}
            />
            {index < history.length - 1 && <div className={styles.line} />}
          </div>

          {/* Contenido */}
          <div className={styles.content}>
            <div className={styles.header}>
              <span className={styles.action}>
                {entry.action === "CREATE"
                  ? "Usuario creado"
                  : entry.action === "DELETE"
                    ? "Usuario eliminado"
                    : "Usuario actualizado"}{" "}
                por
                <strong>{entry.username}</strong>
              </span>
              <span className={styles.time}>{timeAgo(entry.createdAt)}</span>

              <span
                className={`${styles.status} ${
                  entry.status === "SUCCESS"
                    ? styles.statusSuccess
                    : styles.statusFailed
                }`}
                title={`Módulo: ${entry.module}`}
              >
                {entry.status === "SUCCESS" ? "Correcto" : entry.status}
              </span>
            </div>

            {/* Cambios campo a campo */}
            {entry.changes.length > 0 && (
              <ul className={styles.changes}>
                {entry.changes.map((change) => (
                  <li key={change.field} className={styles.change}>
                    <span className={styles.fieldName}>
                      {FIELD_LABELS[change.field] ?? change.field}:
                    </span>
                    <span className={styles.oldValue}>
                      {change.oldValue || "—"}
                    </span>
                    <span className={styles.arrow}>→</span>
                    <span className={styles.newValue}>
                      {change.newValue || "—"}
                    </span>
                  </li>
                ))}
              </ul>
            )}

            {entry.action === "CREATE" && (
              <p className={styles.createMsg}>
                Usuario registrado en el sistema.
              </p>
            )}
          </div>
        </div>
      ))}
    </div>
  );
}
