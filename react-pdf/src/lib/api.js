import axios from "axios";

const BASE_URL = import.meta.env.VITE_API_BASE_URL || "http://localhost:9097/api/pdf";

const client = axios.create({
  baseURL: BASE_URL,
});

/** Encodes a string as UTF-8 safe base64, matching the Base64 decoding the API performs on passwords. */
function toBase64(value) {
  const bytes = new TextEncoder().encode(value);
  let binary = "";
  bytes.forEach((b) => {
    binary += String.fromCharCode(b);
  });
  return btoa(binary);
}

function filenameFromDisposition(disposition, fallback) {
  if (!disposition) return fallback;
  const match = /filename="?([^";]+)"?/i.exec(disposition);
  return match ? match[1] : fallback;
}

/**
 * Runs a multipart POST against the API.
 * responseType 'blob' downloads a file, 'json' parses a ResponseDTO payload.
 */
async function request({ path, formData, responseType, onProgress }) {
  const res = await client.post(path, formData, {
    responseType: responseType === "blob" ? "blob" : "json",
    onUploadProgress: (evt) => {
      if (onProgress && evt.total) {
        onProgress(Math.round((evt.loaded * 100) / evt.total));
      }
    },
  });

  if (responseType === "blob") {
    const disposition = res.headers["content-disposition"];
    return {
      blob: res.data,
      filename: filenameFromDisposition(disposition, "resultado"),
      contentType: res.headers["content-type"],
    };
  }

  return res.data;
}

export const pdfApi = {
  extractText: ({ file, start, end }, onProgress) => {
    const formData = new FormData();
    formData.append("file", file);
    const hasRange = start != null && end != null;
    if (hasRange) {
      formData.append("start", start);
      formData.append("end", end);
    }
    return request({
      path: hasRange ? "/extract/text/pages" : "/extract/text",
      formData,
      responseType: "json",
      onProgress,
    });
  },

  rotate: ({ file, angle, start, end }, onProgress) => {
    const formData = new FormData();
    formData.append("file", file);
    formData.append("angle", angle);
    const hasRange = start != null && end != null;
    if (hasRange) {
      formData.append("start", start);
      formData.append("end", end);
    }
    return request({
      path: hasRange ? "/rotate/pages" : "/rotate",
      formData,
      responseType: "blob",
      onProgress,
    });
  },

  extractPages: ({ file, start, end }, onProgress) => {
    const formData = new FormData();
    formData.append("file", file);
    formData.append("start", start);
    formData.append("end", end);
    return request({
      path: "/extract/document/pages",
      formData,
      responseType: "blob",
      onProgress,
    });
  },

  imageToPdf: ({ file }, onProgress) => {
    const formData = new FormData();
    formData.append("file", file);
    return request({
      path: "/converter/image-to-pdf",
      formData,
      responseType: "blob",
      onProgress,
    });
  },

  merge: ({ files }, onProgress) => {
    const formData = new FormData();
    files.forEach((file) => formData.append("files", file));
    return request({
      path: "/merge/document",
      formData,
      responseType: "blob",
      onProgress,
    });
  },

  split: ({ file, sizePerDocument }, onProgress) => {
    const formData = new FormData();
    formData.append("file", file);
    formData.append("sizePerDocument", sizePerDocument);
    return request({
      path: "/split/document",
      formData,
      responseType: "blob",
      onProgress,
    });
  },

  compress: ({ file }, onProgress) => {
    const formData = new FormData();
    formData.append("file", file);
    return request({
      path: "/compress/document",
      formData,
      responseType: "blob",
      onProgress,
    });
  },

  removePassword: ({ file, password }, onProgress) => {
    const formData = new FormData();
    formData.append("file", file);
    formData.append("password", toBase64(password));
    return request({
      path: "/remove",
      formData,
      responseType: "blob",
      onProgress,
    });
  },

  pdfToImage: ({ file, dpi }, onProgress) => {
    const formData = new FormData();
    formData.append("file", file);
    formData.append("dpi", dpi);
    return request({
      path: "/converter/pdf-to-image",
      formData,
      responseType: "blob",
      onProgress,
    });
  },

  watermark: ({ file, angle, watermark }, onProgress) => {
    const formData = new FormData();
    formData.append("file", file);
    formData.append("angle", angle);
    formData.append("watermark", watermark || "");
    return request({
      path: "/watermark",
      formData,
      responseType: "blob",
      onProgress,
    });
  },
};

export function downloadBlob(blob, filename) {
  const url = URL.createObjectURL(blob);
  const link = document.createElement("a");
  link.href = url;
  link.download = filename;
  document.body.appendChild(link);
  link.click();
  link.remove();
  URL.revokeObjectURL(url);
}

export async function extractErrorMessage(error) {
  const data = error?.response?.data;
  if (data instanceof Blob) {
    try {
      const text = await data.text();
      const parsed = JSON.parse(text);
      return parsed?.message || text || "No se pudo procesar el archivo.";
    } catch {
      return "No se pudo procesar el archivo. Verifica el PDF e inténtalo de nuevo.";
    }
  }
  return data?.message || error?.message || "Ocurrió un error inesperado.";
}
