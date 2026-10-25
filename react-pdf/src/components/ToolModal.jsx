import { useEffect, useMemo, useRef, useState } from "react";
import toast from "react-hot-toast";
import { X, Loader2, Download, Copy, RotateCcw, CheckCircle2, Eye } from "lucide-react";
import Icon from "./Icon";
import FileDrop from "./FileDrop";
import { pdfApi, downloadBlob, extractErrorMessage } from "../lib/api";
import { formatBytes } from "../lib/format";

function buildInitialValues(tool) {
  const values = {};
  tool.fields.forEach((field) => {
    if (field.type === "file") values[field.name] = null;
    else if (field.type === "files") values[field.name] = [];
    else if (field.type === "toggle") values[field.name] = false;
    else values[field.name] = field.default ?? "";
  });
  return values;
}

export default function ToolModal({ tool, onClose }) {
  const [values, setValues] = useState(() => buildInitialValues(tool));
  const [status, setStatus] = useState("idle"); // idle | loading | success | error
  const [progress, setProgress] = useState(0);
  const [error, setError] = useState("");
  const [result, setResult] = useState(null);
  const previewUrlRef = useRef(null);

  useEffect(() => {
    return () => {
      if (previewUrlRef.current) URL.revokeObjectURL(previewUrlRef.current);
    };
  }, []);

  const set = (name, value) => setValues((prev) => ({ ...prev, [name]: value }));

  const visibleFields = useMemo(
    () => tool.fields.filter((f) => !f.showIf || values[f.showIf]),
    [tool.fields, values]
  );

  const isValid = useMemo(() => {
    return visibleFields.every((field) => {
      if (field.type === "file") return !!values[field.name];
      if (field.type === "files") return values[field.name]?.length >= (field.minFiles || 1);
      if (!field.required) return true;
      return values[field.name] !== "" && values[field.name] != null;
    });
  }, [visibleFields, values]);

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!isValid || status === "loading") return;
    setStatus("loading");
    setProgress(0);
    setError("");
    try {
      const data = await tool.run(pdfApi, values, setProgress);
      if (tool.responseType === "json") {
        setResult({ kind: "text", content: data?.content || "" });
      } else {
        const url = URL.createObjectURL(data.blob);
        previewUrlRef.current = url;
        setResult({ kind: "file", url, blob: data.blob, filename: data.filename, contentType: data.contentType });
      }
      setStatus("success");
      toast.success("¡Listo! Tu archivo se procesó correctamente.");
    } catch (err) {
      const message = await extractErrorMessage(err);
      setError(message);
      setStatus("error");
      toast.error(message);
    }
  };

  const reset = () => {
    if (previewUrlRef.current) {
      URL.revokeObjectURL(previewUrlRef.current);
      previewUrlRef.current = null;
    }
    setValues(buildInitialValues(tool));
    setStatus("idle");
    setProgress(0);
    setError("");
    setResult(null);
  };

  const isPdfPreview = result?.kind === "file" && result.contentType?.includes("pdf");

  return (
    <div className="fixed inset-0 z-50 flex items-end justify-center bg-slate-900/50 backdrop-blur-sm animate-fade-in sm:items-center sm:p-4">
      <div className="flex max-h-[92vh] w-full flex-col overflow-hidden rounded-t-3xl bg-white shadow-2xl animate-slide-up dark:bg-slate-900 sm:max-w-lg sm:rounded-3xl">
        <div className="flex items-center gap-3 border-b border-slate-100 px-6 py-4 dark:border-slate-800">
          <span className="flex h-10 w-10 items-center justify-center rounded-xl bg-brand-100 text-brand-600 dark:bg-brand-500/15 dark:text-brand-300">
            <Icon name={tool.icon} className="h-5 w-5" />
          </span>
          <div className="min-w-0 flex-1">
            <h2 className="font-display text-lg font-semibold text-slate-800 dark:text-slate-100">{tool.title}</h2>
            <p className="truncate text-xs text-slate-400 dark:text-slate-500">{tool.description}</p>
          </div>
          <button
            type="button"
            onClick={onClose}
            className="rounded-full p-2 text-slate-400 hover:bg-slate-100 hover:text-slate-600 dark:hover:bg-slate-800"
            aria-label="Cerrar"
          >
            <X className="h-5 w-5" />
          </button>
        </div>

        <div className="scrollbar-thin overflow-y-auto px-6 py-5">
          {status === "success" && result ? (
            <ResultView tool={tool} result={result} isPdfPreview={isPdfPreview} onReset={reset} />
          ) : (
            <form onSubmit={handleSubmit} className="flex flex-col gap-5">
              {tool.fields.map((field) => (
                <FieldRenderer key={field.name} field={field} values={values} set={set} />
              ))}

              {status === "error" && (
                <p className="rounded-xl bg-rose-50 px-3 py-2 text-sm text-rose-600 dark:bg-rose-500/10 dark:text-rose-400">
                  {error}
                </p>
              )}

              {status === "loading" && (
                <div className="h-2 w-full overflow-hidden rounded-full bg-slate-100 dark:bg-slate-800">
                  <div
                    className="h-full rounded-full bg-gradient-to-r from-brand-500 to-accent-500 transition-all duration-200"
                    style={{ width: `${Math.max(progress, 8)}%` }}
                  />
                </div>
              )}

              <button
                type="submit"
                disabled={!isValid || status === "loading"}
                className="flex items-center justify-center gap-2 rounded-xl bg-brand-600 px-4 py-3 font-medium text-white shadow-md shadow-brand-600/20 transition-colors hover:bg-brand-700 disabled:cursor-not-allowed disabled:bg-slate-300 disabled:shadow-none dark:disabled:bg-slate-700"
              >
                {status === "loading" ? (
                  <>
                    <Loader2 className="h-4 w-4 animate-spin" /> Procesando…
                  </>
                ) : (
                  "Procesar"
                )}
              </button>
            </form>
          )}
        </div>
      </div>
    </div>
  );
}

function FieldRenderer({ field, values, set }) {
  if (field.showIf && !values[field.showIf]) return null;

  if (field.type === "file") {
    return (
      <div>
        <Label field={field} />
        <FileDrop
          accept={field.accept}
          files={values[field.name] ? [values[field.name]] : []}
          onChange={(list) => set(field.name, list[0] || null)}
        />
      </div>
    );
  }

  if (field.type === "files") {
    return (
      <div>
        <Label field={field} />
        <FileDrop multiple files={values[field.name]} onChange={(list) => set(field.name, list)} />
      </div>
    );
  }

  if (field.type === "toggle") {
    return (
      <label className="flex cursor-pointer items-center justify-between rounded-xl border border-slate-200 px-4 py-3 dark:border-slate-700">
        <span className="text-sm font-medium text-slate-700 dark:text-slate-200">{field.label}</span>
        <span className="relative inline-flex h-6 w-11 shrink-0 items-center">
          <input
            type="checkbox"
            className="peer sr-only"
            checked={!!values[field.name]}
            onChange={(e) => set(field.name, e.target.checked)}
          />
          <span className="absolute inset-0 rounded-full bg-slate-200 transition-colors peer-checked:bg-brand-600 dark:bg-slate-700" />
          <span className="absolute left-0.5 h-5 w-5 rounded-full bg-white shadow transition-transform peer-checked:translate-x-5" />
        </span>
      </label>
    );
  }

  if (field.type === "select") {
    return (
      <div>
        <Label field={field} />
        <select
          value={values[field.name]}
          onChange={(e) => set(field.name, e.target.value)}
          className="w-full rounded-xl border border-slate-200 bg-white px-3 py-2.5 text-sm text-slate-700 outline-none ring-brand-500/30 focus:border-brand-500 focus:ring-4 dark:border-slate-700 dark:bg-slate-800 dark:text-slate-100"
        >
          {field.options.map((opt) => (
            <option key={opt.value} value={opt.value}>
              {opt.label}
            </option>
          ))}
        </select>
      </div>
    );
  }

  if (field.type === "number") {
    return (
      <div>
        <Label field={field} />
        <input
          type="number"
          min={field.min}
          max={field.max}
          value={values[field.name]}
          onChange={(e) => set(field.name, e.target.value === "" ? "" : Number(e.target.value))}
          className="w-full rounded-xl border border-slate-200 bg-white px-3 py-2.5 text-sm text-slate-700 outline-none ring-brand-500/30 focus:border-brand-500 focus:ring-4 dark:border-slate-700 dark:bg-slate-800 dark:text-slate-100"
        />
      </div>
    );
  }

  if (field.type === "password") {
    return (
      <div>
        <Label field={field} />
        <input
          type="password"
          autoComplete="off"
          placeholder={field.placeholder}
          value={values[field.name]}
          onChange={(e) => set(field.name, e.target.value)}
          className="w-full rounded-xl border border-slate-200 bg-white px-3 py-2.5 text-sm text-slate-700 outline-none ring-brand-500/30 focus:border-brand-500 focus:ring-4 dark:border-slate-700 dark:bg-slate-800 dark:text-slate-100"
        />
      </div>
    );
  }

  return (
    <div>
      <Label field={field} />
      <input
        type="text"
        placeholder={field.placeholder}
        value={values[field.name]}
        onChange={(e) => set(field.name, e.target.value)}
        className="w-full rounded-xl border border-slate-200 bg-white px-3 py-2.5 text-sm text-slate-700 outline-none ring-brand-500/30 focus:border-brand-500 focus:ring-4 dark:border-slate-700 dark:bg-slate-800 dark:text-slate-100"
      />
    </div>
  );
}

function Label({ field }) {
  return (
    <label className="mb-1.5 block text-sm font-medium text-slate-700 dark:text-slate-200">
      {field.label}
      {field.required && <span className="ml-0.5 text-rose-500">*</span>}
    </label>
  );
}

function ResultView({ tool, result, isPdfPreview, onReset }) {
  const handleDownload = () => {
    if (result.kind === "file") downloadBlob(result.blob, result.filename);
  };

  const handleCopy = async () => {
    await navigator.clipboard.writeText(result.content);
    toast.success("Texto copiado al portapapeles");
  };

  const handleDownloadText = () => {
    const blob = new Blob([result.content], { type: "text/plain;charset=utf-8" });
    downloadBlob(blob, `${tool.id}_texto.txt`);
  };

  return (
    <div className="flex flex-col gap-4 animate-scale-in">
      <div className="flex items-center gap-2 rounded-xl bg-emerald-50 px-4 py-3 text-emerald-700 dark:bg-emerald-500/10 dark:text-emerald-400">
        <CheckCircle2 className="h-5 w-5 shrink-0" />
        <p className="text-sm font-medium">Proceso completado con éxito</p>
      </div>

      {result.kind === "text" && (
        <textarea
          readOnly
          value={result.content}
          className="scrollbar-thin h-56 w-full resize-none rounded-xl border border-slate-200 bg-slate-50 p-3 font-mono text-xs text-slate-700 outline-none dark:border-slate-700 dark:bg-slate-800 dark:text-slate-200"
        />
      )}

      {result.kind === "file" && (
        <div className="rounded-xl border border-slate-200 bg-slate-50 p-4 dark:border-slate-700 dark:bg-slate-800">
          <p className="truncate text-sm font-medium text-slate-700 dark:text-slate-200">{result.filename}</p>
          <p className="text-xs text-slate-400">{formatBytes(result.blob.size)}</p>
          {isPdfPreview && (
            <div className="mt-3 flex items-center gap-1.5 text-xs text-slate-400">
              <Eye className="h-3.5 w-3.5" /> Vista previa disponible al descargar
            </div>
          )}
        </div>
      )}

      <div className="flex gap-2">
        {result.kind === "text" ? (
          <>
            <button
              onClick={handleCopy}
              className="flex flex-1 items-center justify-center gap-2 rounded-xl border border-slate-200 px-4 py-2.5 text-sm font-medium text-slate-700 hover:bg-slate-50 dark:border-slate-700 dark:text-slate-200 dark:hover:bg-slate-800"
            >
              <Copy className="h-4 w-4" /> Copiar
            </button>
            <button
              onClick={handleDownloadText}
              className="flex flex-1 items-center justify-center gap-2 rounded-xl bg-brand-600 px-4 py-2.5 text-sm font-medium text-white hover:bg-brand-700"
            >
              <Download className="h-4 w-4" /> Descargar .txt
            </button>
          </>
        ) : (
          <button
            onClick={handleDownload}
            className="flex flex-1 items-center justify-center gap-2 rounded-xl bg-brand-600 px-4 py-2.5 text-sm font-medium text-white hover:bg-brand-700"
          >
            <Download className="h-4 w-4" /> Descargar resultado
          </button>
        )}
      </div>

      <button
        onClick={onReset}
        className="flex items-center justify-center gap-2 rounded-xl px-4 py-2 text-sm font-medium text-slate-500 hover:text-brand-600 dark:text-slate-400"
      >
        <RotateCcw className="h-4 w-4" /> Procesar otro archivo
      </button>
    </div>
  );
}
