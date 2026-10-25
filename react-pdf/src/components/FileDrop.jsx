import { useCallback, useRef, useState } from "react";
import { UploadCloud, X, FileText } from "lucide-react";
import { formatBytes } from "../lib/format";

export default function FileDrop({ multiple = false, accept, files, onChange }) {
  const [isDragging, setIsDragging] = useState(false);
  const inputRef = useRef(null);
  const list = files || [];

  const addFiles = useCallback(
    (incoming) => {
      const next = Array.from(incoming);
      if (multiple) {
        onChange([...list, ...next]);
      } else {
        onChange(next.slice(0, 1));
      }
    },
    [list, multiple, onChange]
  );

  const handleDrop = (e) => {
    e.preventDefault();
    setIsDragging(false);
    if (e.dataTransfer.files?.length) addFiles(e.dataTransfer.files);
  };

  const removeAt = (idx) => {
    onChange(list.filter((_, i) => i !== idx));
  };

  return (
    <div>
      <button
        type="button"
        onClick={() => inputRef.current?.click()}
        onDragOver={(e) => {
          e.preventDefault();
          setIsDragging(true);
        }}
        onDragLeave={() => setIsDragging(false)}
        onDrop={handleDrop}
        className={`group flex w-full flex-col items-center justify-center gap-2 rounded-2xl border-2 border-dashed px-6 py-10 text-center transition-colors
          ${isDragging
            ? "border-brand-500 bg-brand-50 dark:bg-brand-500/10"
            : "border-slate-300 hover:border-brand-400 hover:bg-slate-50 dark:border-slate-700 dark:hover:border-brand-500 dark:hover:bg-slate-800/50"
          }`}
      >
        <span className="flex h-12 w-12 items-center justify-center rounded-full bg-brand-100 text-brand-600 transition-transform group-hover:scale-105 dark:bg-brand-500/15 dark:text-brand-300">
          <UploadCloud className="h-6 w-6" />
        </span>
        <p className="font-medium text-slate-700 dark:text-slate-200">
          Arrastra tu {multiple ? "archivos" : "archivo"} aquí o haz clic para seleccionar
        </p>
        <p className="text-sm text-slate-400 dark:text-slate-500">
          {accept === "image/*" ? "JPG, PNG, WEBP" : "PDF"} {multiple ? "· puedes elegir varios" : ""}
        </p>
        <input
          ref={inputRef}
          type="file"
          accept={accept}
          multiple={multiple}
          className="hidden"
          onChange={(e) => e.target.files?.length && addFiles(e.target.files)}
        />
      </button>

      {list.length > 0 && (
        <ul className="mt-3 flex flex-col gap-2 animate-fade-in">
          {list.map((file, idx) => (
            <li
              key={`${file.name}-${idx}`}
              className="flex items-center gap-3 rounded-xl border border-slate-200 bg-white px-3 py-2 text-sm shadow-sm dark:border-slate-700 dark:bg-slate-800"
            >
              <FileText className="h-4 w-4 shrink-0 text-brand-500" />
              <span className="min-w-0 flex-1 truncate text-slate-700 dark:text-slate-200">{file.name}</span>
              <span className="shrink-0 text-xs text-slate-400">{formatBytes(file.size)}</span>
              <button
                type="button"
                onClick={(e) => {
                  e.stopPropagation();
                  removeAt(idx);
                }}
                className="shrink-0 rounded-full p-1 text-slate-400 hover:bg-slate-100 hover:text-rose-500 dark:hover:bg-slate-700"
                aria-label="Quitar archivo"
              >
                <X className="h-4 w-4" />
              </button>
            </li>
          ))}
        </ul>
      )}
    </div>
  );
}
