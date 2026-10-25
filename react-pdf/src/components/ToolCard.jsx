import Icon from "./Icon";

const ACCENTS = {
  organizar: "from-brand-500 to-brand-600 text-brand-600 bg-brand-50 dark:bg-brand-500/10 dark:text-brand-300",
  convertir: "from-accent-500 to-accent-600 text-accent-600 bg-accent-50 dark:bg-accent-500/10 dark:text-accent-300",
  editar: "from-violet-500 to-violet-600 text-violet-600 bg-violet-50 dark:bg-violet-500/10 dark:text-violet-300",
  seguridad: "from-rose-500 to-rose-600 text-rose-600 bg-rose-50 dark:bg-rose-500/10 dark:text-rose-300",
  optimizar: "from-emerald-500 to-emerald-600 text-emerald-600 bg-emerald-50 dark:bg-emerald-500/10 dark:text-emerald-300",
};

export default function ToolCard({ tool, onClick, style }) {
  const accent = ACCENTS[tool.category] || ACCENTS.organizar;
  const [bgClass] = accent.split(" ").filter((c) => c.includes("bg-"));

  return (
    <button
      type="button"
      onClick={onClick}
      style={style}
      className="group relative flex animate-slide-up flex-col items-start gap-4 rounded-2xl border border-slate-200 bg-white p-5 text-left shadow-sm transition-all duration-200 hover:-translate-y-1 hover:shadow-lg hover:shadow-slate-200/60 dark:border-slate-800 dark:bg-slate-900 dark:hover:shadow-black/30"
    >
      <span
        className={`flex h-11 w-11 items-center justify-center rounded-xl ${accent} transition-transform duration-200 group-hover:scale-110`}
      >
        <Icon name={tool.icon} className="h-5.5 w-5.5" />
      </span>
      <div>
        <h3 className="font-display text-base font-semibold text-slate-800 dark:text-slate-100">{tool.title}</h3>
        <p className="mt-1 text-sm leading-snug text-slate-500 dark:text-slate-400">{tool.description}</p>
      </div>
      <span className="absolute right-4 top-4 text-xs font-medium text-slate-300 opacity-0 transition-opacity group-hover:opacity-100 dark:text-slate-600">
        Abrir →
      </span>
    </button>
  );
}
