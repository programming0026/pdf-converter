import { FileStack, Moon, Sun } from "lucide-react";

export default function Header({ isDark, onToggleDark }) {
  return (
    <header className="sticky top-0 z-40 border-b border-slate-200/70 bg-white/80 backdrop-blur-md dark:border-slate-800/70 dark:bg-slate-950/80">
      <div className="mx-auto flex max-w-6xl items-center justify-between px-4 py-3 sm:px-6">
        <div className="flex items-center gap-2.5">
          <span className="flex h-9 w-9 items-center justify-center rounded-xl bg-gradient-to-br from-brand-500 to-accent-500 text-white shadow-md shadow-brand-500/30">
            <FileStack className="h-5 w-5" />
          </span>
          <span className="font-display text-lg font-bold tracking-tight text-slate-800 dark:text-slate-100">
            PDF Studio
          </span>
        </div>
        <button
          type="button"
          onClick={onToggleDark}
          aria-label="Cambiar tema"
          className="flex h-9 w-9 items-center justify-center rounded-full border border-slate-200 text-slate-500 transition-colors hover:bg-slate-100 dark:border-slate-700 dark:text-slate-300 dark:hover:bg-slate-800"
        >
          {isDark ? <Sun className="h-4.5 w-4.5" /> : <Moon className="h-4.5 w-4.5" />}
        </button>
      </div>
    </header>
  );
}
