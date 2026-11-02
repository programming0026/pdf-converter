# PDF Studio

Aplicación web para **convertir, editar y organizar archivos PDF** desde el navegador. Está formada por un frontend en React y una API REST en Spring Boot, orquestados con Docker Compose.

| Módulo | Descripción | Tecnología | Puerto |
|---|---|---|---|
| [`react-pdf`](./react-pdf/README.md) | Interfaz web (catálogo de herramientas, modales, tema claro/oscuro) | React 19 · Vite · Tailwind CSS 4 | `3003` (Docker) / `5173` (dev) |
| [`sbj-pdf`](./sbj-pdf/README.md) | API REST que procesa los PDF | Java 21 · Spring Boot 4 · Apache PDFBox | `9097` |

## Capturas de pantalla

| Tema claro | Tema oscuro |
|---|---|
| ![Inicio claro](docs/screenshots/home-light.png) | ![Inicio oscuro](docs/screenshots/home-dark.png) |

**Filtro por categoría** (ejemplo: *Seguridad*)

![Filtro por categoría](docs/screenshots/filter-seguridad.png)

**Flujo de una herramienta** (ejemplo: *Rotar PDF*): formulario → archivo seleccionado → resultado

| 1. Formulario | 2. Archivo cargado | 3. Resultado |
|---|---|---|
| ![Modal](docs/screenshots/modal-rotar.png) | ![Archivo](docs/screenshots/modal-rotar-file.png) | ![Resultado](docs/screenshots/modal-rotar-result.png) |

**Documentación interactiva de la API** (Swagger UI en `http://localhost:9097/swagger-ui/index.html`)

![Swagger](docs/screenshots/swagger.png)

## Herramientas disponibles

| Categoría | Herramienta | Endpoint |
|---|---|---|
| Organizar | Combinar PDF | `POST /api/pdf/merge/document` |
| Organizar | Dividir PDF (por tamaño máximo, devuelve ZIP) | `POST /api/pdf/split/document` |
| Organizar | Extraer páginas | `POST /api/pdf/extract/document/pages` |
| Organizar | Rotar PDF (todo o por rango) | `POST /api/pdf/rotate` · `/rotate/pages` |
| Convertir | Imagen a PDF | `POST /api/pdf/converter/image-to-pdf` |
| Convertir | PDF a imágenes (ZIP, DPI configurable) | `POST /api/pdf/converter/pdf-to-image` |
| Editar | Extraer texto (todo o por rango) | `POST /api/pdf/extract/text` · `/extract/text/pages` |
| Editar | Marca de agua | `POST /api/pdf/watermark` |
| Seguridad | Quitar contraseña | `POST /api/pdf/remove` |
| Optimizar | Comprimir PDF | `POST /api/pdf/compress/document` |

## Arquitectura

### Diagrama de componentes

```mermaid
flowchart LR
    U([Usuario]) -->|navegador| FE

    subgraph Docker["Docker Compose · app-network"]
        FE["pdf-studio-frontend<br/>React + Vite (serve)<br/>:3003 → 3000"]
        BE["pdf-studio-backend<br/>Spring Boot 4 · Java 21<br/>:9097"]
    end

    FE -->|"HTTP multipart/form-data<br/>(axios) /api/pdf/*"| BE
    BE --> PDFBOX[(Apache PDFBox)]
    BE -.->|documentación| SW[Swagger UI<br/>/swagger-ui/index.html]
```

### Diagrama de despliegue

```mermaid
flowchart TB
    subgraph Host["Equipo local (Docker Desktop)"]
        subgraph C1["Contenedor pdf-studio-frontend"]
            N["node:26.13.0-alpine<br/>serve -s dist"]
        end
        subgraph C2["Contenedor pdf-studio-backend"]
            J["amazoncorretto:21-alpine<br/>app.jar (run.sh)"]
        end
    end
    B([Navegador]) -- "localhost:3003" --> N
    B -- "localhost:9097/api/pdf" --> J
```

### Diagrama de clases — backend (`sbj-pdf`)

```mermaid
classDiagram
    class PdfController {
        -PdfEditService pdfEditService
        -PdfTextService pdfTextService
        -PdfConversionService pdfConversionService
        -PdfSecurityService pdfSecurityService
        -PdfOptimizeService pdfOptimizeService
        -ZipService zipService
        +extractText(file) ResponseEntity
        +extractText(file, start, end) ResponseEntity
        +rotate(file, angle, response)
        +rotate(file, angle, start, end, response)
        +extractDocument(file, start, end, response)
        +converterImageToPdf(file, response)
        +merge(files, response)
        +split(file, size, response)
        +compress(file, response)
        +removePwd(file, password, response)
        +converterPdfToImage(file, dpi, response)
        +watermark(file, angle, text, response)
    }
    class PdfEditService {
        <<interface>>
        +rotate(...)
        +extractPages(...)
        +merge(files, out)
        +split(file, maxSize) List~FilesDTO~
        +watermark(file, out, text, angle)
    }
    class PdfTextService {
        <<interface>>
        +extractText(...) String
    }
    class PdfConversionService {
        <<interface>>
        +imageToPdf(file, out)
        +pdfToImage(file, dpi) List~FilesDTO~
    }
    class PdfSecurityService {
        <<interface>>
        +removePassword(file, out, pwd)
    }
    class PdfOptimizeService {
        <<interface>>
        +compress(file, out)
    }
    class ZipService {
        <<interface>>
        +zip(files, out)
    }
    class ZipServiceImpl
    class FilesDTO {
        -String fileName
        -String file
    }
    class ResponseDTO~T~ {
        -T content
        -String message
    }
    class Angles {
        <<enumeration>>
        DEGREE_45
        DEGREE_90
        DEGREE_180
        DEGREE_270
        DEGREE_360
        +getDegree() Integer
    }
    class FileTools {
        <<utility>>
        +getExtension(name, ext) String
    }

    PdfController --> PdfEditService
    PdfController --> PdfTextService
    PdfController --> PdfConversionService
    PdfController --> PdfSecurityService
    PdfController --> PdfOptimizeService
    PdfController --> ZipService
    PdfController ..> Angles
    PdfController ..> FileTools
    PdfController ..> ResponseDTO
    PdfEditServiceImpl ..|> PdfEditService
    PdfTextServiceImpl ..|> PdfTextService
    PdfConversionServiceImpl ..|> PdfConversionService
    PdfSecurityServiceImpl ..|> PdfSecurityService
    PdfOptimizeServiceImpl ..|> PdfOptimizeService
    ZipServiceImpl ..|> ZipService
    PdfEditService ..> FilesDTO
    PdfConversionService ..> FilesDTO
    ZipService ..> FilesDTO
```

### Diagrama de componentes — frontend (`react-pdf`)

```mermaid
classDiagram
    class App {
        -isDark
        -query
        -category
        -activeToolId
    }
    class Header
    class ToolCard
    class ToolModal {
        -values
        -status
        -progress
        -result
        +handleSubmit()
    }
    class FileDrop
    class Icon
    class tools_js {
        <<module>>
        +CATEGORIES
        +TOOLS
    }
    class api_js {
        <<module>>
        +pdfApi
        +downloadBlob()
        +extractErrorMessage()
    }
    class format_js {
        <<module>>
        +formatBytes()
    }

    App --> Header
    App --> ToolCard
    App --> ToolModal
    App ..> tools_js
    ToolCard --> Icon
    ToolModal --> FileDrop
    ToolModal --> Icon
    ToolModal ..> api_js
    ToolModal ..> format_js
    tools_js ..> api_js : tool.run()
```

### Diagrama de secuencia — procesar un PDF (rotar)

```mermaid
sequenceDiagram
    actor U as Usuario
    participant App as App / ToolCard
    participant M as ToolModal
    participant API as pdfApi (axios)
    participant C as PdfController
    participant S as PdfEditServiceImpl
    participant P as PDFBox

    U->>App: Click en "Rotar PDF"
    App->>M: abre modal (tool)
    U->>M: sube PDF + elige ángulo
    U->>M: "Procesar"
    M->>API: tool.run(values, onProgress)
    API->>C: POST /api/pdf/rotate (multipart)
    C->>S: rotate(file, out, grados)
    S->>P: Loader.loadPDF → page.setRotation → save
    P-->>S: documento rotado
    S-->>C: escribe en OutputStream
    C-->>API: 200 application/pdf + Content-Disposition
    API-->>M: { blob, filename, contentType }
    M-->>U: resultado + botón "Descargar"
```

### Casos de uso

```mermaid
flowchart LR
    U((Usuario))
    U --- A[Combinar PDF]
    U --- B[Dividir PDF]
    U --- C[Extraer páginas]
    U --- D[Rotar PDF]
    U --- E[Imagen a PDF]
    U --- F[PDF a imágenes]
    U --- G[Extraer texto]
    U --- H[Marca de agua]
    U --- I[Quitar contraseña]
    U --- J[Comprimir PDF]
    U --- K[Alternar tema claro/oscuro]
    U --- L[Buscar / filtrar herramientas]
```

## Puesta en marcha

### Con Docker (recomendado)

Requisitos: Docker y Docker Compose.

```bash
docker compose up -d --build
```

| Servicio | URL |
|---|---|
| Frontend | http://localhost:3003 |
| API | http://localhost:9097/api/pdf |
| Swagger UI | http://localhost:9097/swagger-ui/index.html |

Detener y limpiar:

```bash
docker compose down
```

### Sin Docker (desarrollo)

```bash
# Backend (Java 21 + Maven)
cd sbj-pdf
mvn spring-boot:run            # http://localhost:9097

# Frontend (Node 20+)
cd react-pdf
npm install
npm run dev                    # http://localhost:5173
```

El frontend lee la URL de la API de `VITE_API_BASE_URL` (por defecto `http://localhost:9097/api/pdf`, ver `react-pdf/.env`).

## Estructura del repositorio

```
pdf_converter/
├── docker-compose.yml      # frontend + backend en la red app-network
├── docs/screenshots/       # capturas usadas en este README
├── react-pdf/              # frontend (React + Vite + Tailwind)
└── sbj-pdf/                # backend (Spring Boot + PDFBox)
```

## Notas

- El tamaño máximo de subida es de **100 MB** por archivo y por petición (`application.properties`).
- La contraseña de *Quitar contraseña* viaja en **Base64** (codificación, no cifrado); usa HTTPS si lo expones fuera de localhost.
- La URL de la API se define en build con `VITE_API_BASE_URL` (por defecto `http://localhost:9097/api/pdf`). Para cambiarla: `VITE_API_BASE_URL=https://mi-host/api/pdf docker compose up -d --build`. Debe ser accesible desde el navegador, no el hostname interno de Docker.
