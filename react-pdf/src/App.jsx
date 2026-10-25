import { useEffect, useMemo, useState } from "react";
import { Toaster } from "react-hot-toast";
import { Search, Sparkles } from "lucide-react";
import Header from "./components/Header";
import ToolCard from "./components/ToolCard";
import ToolModal from "./components/ToolModal";
import { CATEGORIES, TOOLS } from "./lib/tools";

function usePrefersDark() {
  const [isDark, setIsDark] = useState(() => {
    const stored = localStorage.getItem("pdf-studio-theme");
    if (stored) return stored === "dark";
    return window.matchMedia?.("(prefers-color-scheme: dark)").matches ?? false;
  });

  useEffect(() => {
    document.documentElement.classList.toggle("dark", isDark);
    localStorage.setItem("pdf-studio-theme", isDark ? "dark" : "light");
  }, [isDark]);

  return [isDark, setIsDark];
}

export default function App() {
  const [isDark, setIsDark] = usePrefersDark();
  const [query, setQuery] = useState("");
  const [category, setCategory] = useState("todas");
  const [activeToolId, setActiveToolId] = useState(null);

  const filteredTools = useMemo(() => {
    return TOOLS.filter((tool) => {
      const matchesCategory = category === "todas" || tool.category === category;
      const q = query.trim().toLowerCase();
      const matchesQuery = !q || tool.title.toLowerCase().includes(q) || tool.description.toLowerCase().includes(q);
      return matchesCategory && matchesQuery;
    });
  }, [query, category]);

  const activeTool = TOOLS.find((t) => t.id === activeToolId);

  return (
    <div className="min-h-screen bg-slate-50 dark:bg-slate-950">
      <Header isDark={isDark} onToggleDark={() => setIsDark((v) => !v)} />

      <main className="mx-auto max-w-6xl px-4 pb-20 sm:px-6">
        <section className="relative overflow-hidden py-14 text-center sm:py-20">
          <div className="pointer-events-none absolute inset-0 -z-10">
            <div className="absolute left-1/2 top-0 h-72 w-[36rem] -translate-x-1/2 rounded-full bg-gradient-to-br from-brand-400/30 to-accent-400/30 blur-3xl dark:from-brand-500/20 dark:to-accent-500/20" />
          </div>

          <span className="mx-auto mb-4 inline-flex items-center gap-1.5 rounded-full border border-brand-200 bg-brand-50 px-3 py-1 text-xs font-medium text-brand-700 dark:border-brand-500/30 dark:bg-brand-500/10 dark:text-brand-300">
            <Sparkles className="h-3.5 w-3.5" /> Todo tu PDF, en un solo lugar
          </span>
          <h1 className="font-display text-4xl font-extrabold tracking-tight text-slate-900 sm:text-5xl dark:text-white">
            Convierte, edita y organiza
            <br className="hidden sm:block" /> tus{" "}
            <span className="bg-gradient-to-r from-brand-600 to-accent-500 bg-clip-text text-transparent">
              PDF en segundos
            </span>
          </h1>
          <p className="mx-auto mt-4 max-w-xl text-slate-500 dark:text-slate-400">
            Combina, divide, comprime, rota y protege tus documentos directamente desde el navegador.
          </p>

          <div className="mx-auto mt-8 flex max-w-md items-center gap-2 rounded-2xl border border-slate-200 bg-white px-4 py-3 shadow-sm dark:border-slate-800 dark:bg-slate-900">
            <Search className="h-4.5 w-4.5 shrink-0 text-slate-400" />
            <input
              value={query}
              onChange={(e) => setQuery(e.target.value)}
              placeholder="Buscar una herramienta… (ej. rotar, comprimir)"
              className="w-full bg-transparent text-sm text-slate-700 outline-none placeholder:text-slate-400 dark:text-slate-100"
            />
          </div>

          <div className="mt-6 flex flex-wrap justify-center gap-2">
            <CategoryPill active={category === "todas"} onClick={() => setCategory("todas")} label="Todas" />
            {CATEGORIES.map((c) => (
              <CategoryPill key={c.id} active={category === c.id} onClick={() => setCategory(c.id)} label={c.label} />
            ))}
          </div>
        </section>

        {filteredTools.length > 0 ? (
          <section className="grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-3">
            {filteredTools.map((tool, idx) => (
              <ToolCard
                key={tool.id}
                tool={tool}
                style={{ animationDelay: `${idx * 40}ms` }}
                onClick={() => setActiveToolId(tool.id)}
              />
            ))}
          </section>
        ) : (
          <p className="py-16 text-center text-slate-400">No encontramos herramientas para "{query}".</p>
        )}
      </main>

      <footer className="border-t border-slate-200 py-6 text-center text-xs text-slate-400 dark:border-slate-800">
        PDF Studio &bull; Copyright &copy; {new Date().getFullYear()}
      </footer>

      {activeTool && <ToolModal tool={activeTool} onClose={() => setActiveToolId(null)} />}

      <Toaster position="bottom-center" toastOptions={{ style: { fontSize: "0.875rem" } }} />
    </div>
  );
}

function CategoryPill({ active, onClick, label }) {
  return (
    <button
      type="button"
      onClick={onClick}
      className={`rounded-full px-3.5 py-1.5 text-sm font-medium transition-colors ${
        active
          ? "bg-brand-600 text-white shadow-sm shadow-brand-600/30"
          : "bg-white text-slate-500 hover:bg-slate-100 dark:bg-slate-900 dark:text-slate-400 dark:hover:bg-slate-800"
      }`}
    >
      {label}
    </button>
  );
}
