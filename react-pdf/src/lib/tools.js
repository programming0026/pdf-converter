export const ANGLE_OPTIONS = [
  { value: "DEGREE_45", label: "45°" },
  { value: "DEGREE_90", label: "90°" },
  { value: "DEGREE_180", label: "180°" },
  { value: "DEGREE_270", label: "270°" },
  { value: "DEGREE_360", label: "360°" },
];

export const CATEGORIES = [
  { id: "organizar", label: "Organizar", color: "brand" },
  { id: "convertir", label: "Convertir", color: "accent" },
  { id: "editar", label: "Editar", color: "brand" },
  { id: "seguridad", label: "Seguridad", color: "rose" },
  { id: "optimizar", label: "Optimizar", color: "emerald" },
];

/**
 * Declarative description of every operation exposed by the sfb-pdf API.
 * `fields` drives the generic form renderer in ToolModal.
 */
export const TOOLS = [
  {
    id: "merge",
    title: "Combinar PDF",
    description: "Une varios PDF en un único documento, en el orden que elijas.",
    category: "organizar",
    icon: "Combine",
    accept: "application/pdf",
    multiple: true,
    fields: [{ name: "files", type: "files", label: "Archivos PDF", required: true, minFiles: 2 }],
    responseType: "blob",
    run: (api, values, onProgress) => api.merge({ files: values.files }, onProgress),
  },
  {
    id: "split",
    title: "Dividir PDF",
    description: "Divide un PDF grande en varios archivos por tamaño máximo.",
    category: "organizar",
    icon: "Scissors",
    accept: "application/pdf",
    fields: [
      { name: "file", type: "file", label: "Archivo PDF", required: true },
      {
        name: "sizePerDocumentMB",
        type: "number",
        label: "Tamaño máximo por archivo (MB)",
        default: 1,
        min: 1,
        max: 100,
      },
    ],
    responseType: "blob",
    run: (api, values, onProgress) =>
      api.split(
        { file: values.file, sizePerDocument: Math.round(values.sizePerDocumentMB * 1024 * 1024) },
        onProgress
      ),
  },
  {
    id: "extractPages",
    title: "Extraer páginas",
    description: "Obtén un nuevo PDF con solo el rango de páginas que necesitas.",
    category: "organizar",
    icon: "FileOutput",
    accept: "application/pdf",
    fields: [
      { name: "file", type: "file", label: "Archivo PDF", required: true },
      { name: "start", type: "number", label: "Página inicial", default: 1, min: 1, required: true },
      { name: "end", type: "number", label: "Página final", default: 1, min: 1, required: true },
    ],
    responseType: "blob",
    run: (api, values, onProgress) =>
      api.extractPages({ file: values.file, start: values.start, end: values.end }, onProgress),
  },
  {
    id: "rotate",
    title: "Rotar PDF",
    description: "Gira todas las páginas o solo un rango, en el ángulo que quieras.",
    category: "organizar",
    icon: "RotateCw",
    accept: "application/pdf",
    fields: [
      { name: "file", type: "file", label: "Archivo PDF", required: true },
      { name: "angle", type: "select", label: "Ángulo de rotación", options: ANGLE_OPTIONS, default: "DEGREE_90" },
      { name: "usePageRange", type: "toggle", label: "Aplicar solo a un rango de páginas" },
      { name: "start", type: "number", label: "Página inicial", default: 1, min: 1, showIf: "usePageRange" },
      { name: "end", type: "number", label: "Página final", default: 1, min: 1, showIf: "usePageRange" },
    ],
    responseType: "blob",
    run: (api, values, onProgress) =>
      api.rotate(
        {
          file: values.file,
          angle: values.angle,
          start: values.usePageRange ? values.start : null,
          end: values.usePageRange ? values.end : null,
        },
        onProgress
      ),
  },
  {
    id: "imageToPdf",
    title: "Imagen a PDF",
    description: "Convierte una foto o imagen escaneada en un documento PDF.",
    category: "convertir",
    icon: "ImageIcon",
    accept: "image/*",
    fields: [{ name: "file", type: "file", label: "Imagen", required: true }],
    responseType: "blob",
    run: (api, values, onProgress) => api.imageToPdf({ file: values.file }, onProgress),
  },
  {
    id: "pdfToImage",
    title: "PDF a imágenes",
    description: "Exporta cada página como una imagen dentro de un ZIP.",
    category: "convertir",
    icon: "Images",
    accept: "application/pdf",
    fields: [
      { name: "file", type: "file", label: "Archivo PDF", required: true },
      { name: "dpi", type: "number", label: "Resolución (DPI)", default: 300, min: 72, max: 600 },
    ],
    responseType: "blob",
    run: (api, values, onProgress) => api.pdfToImage({ file: values.file, dpi: values.dpi }, onProgress),
  },
  {
    id: "extractText",
    title: "Extraer texto",
    description: "Obtén el texto plano de todo el documento o de un rango de páginas.",
    category: "editar",
    icon: "FileText",
    accept: "application/pdf",
    fields: [
      { name: "file", type: "file", label: "Archivo PDF", required: true },
      { name: "usePageRange", type: "toggle", label: "Limitar a un rango de páginas" },
      { name: "start", type: "number", label: "Página inicial", default: 1, min: 1, showIf: "usePageRange" },
      { name: "end", type: "number", label: "Página final", default: 1, min: 1, showIf: "usePageRange" },
    ],
    responseType: "json",
    run: (api, values, onProgress) =>
      api.extractText(
        {
          file: values.file,
          start: values.usePageRange ? values.start : null,
          end: values.usePageRange ? values.end : null,
        },
        onProgress
      ),
  },
  {
    id: "watermark",
    title: "Marca de agua",
    description: "Añade un texto en diagonal sobre cada página del documento.",
    category: "editar",
    icon: "Droplets",
    accept: "application/pdf",
    fields: [
      { name: "file", type: "file", label: "Archivo PDF", required: true },
      { name: "watermark", type: "text", label: "Texto de la marca de agua", placeholder: "Ej. CONFIDENCIAL", required: true },
      { name: "angle", type: "select", label: "Ángulo del texto", options: ANGLE_OPTIONS, default: "DEGREE_45" },
    ],
    responseType: "blob",
    run: (api, values, onProgress) =>
      api.watermark({ file: values.file, angle: values.angle, watermark: values.watermark }, onProgress),
  },
  {
    id: "removePassword",
    title: "Quitar contraseña",
    description: "Elimina la contraseña de apertura de un PDF protegido.",
    category: "seguridad",
    icon: "Unlock",
    accept: "application/pdf",
    fields: [
      { name: "file", type: "file", label: "Archivo PDF", required: true },
      { name: "password", type: "password", label: "Contraseña actual", required: true },
    ],
    responseType: "blob",
    run: (api, values, onProgress) =>
      api.removePassword({ file: values.file, password: values.password }, onProgress),
  },
  {
    id: "compress",
    title: "Comprimir PDF",
    description: "Reduce el tamaño del archivo manteniendo la mejor calidad posible.",
    category: "optimizar",
    icon: "Minimize2",
    accept: "application/pdf",
    fields: [{ name: "file", type: "file", label: "Archivo PDF", required: true }],
    responseType: "blob",
    run: (api, values, onProgress) => api.compress({ file: values.file }, onProgress),
  },
];

export function getTool(id) {
  return TOOLS.find((t) => t.id === id);
}
