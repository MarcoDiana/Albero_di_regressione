/**
 * Helper client-side per il rendering interattivo del diagramma dell'Albero di Regressione
 * tramite la libreria Mermaid.js in ambiente Vaadin Flow.
 *
 * @author Marco Diana
 * @version 1.0
 */
window.TreeViewer = {
    render: async function(containerId, mermaidCode) {
        const container = document.getElementById(containerId);
        if (!container) {
            console.warn("TreeViewer: container non trovato con id=" + containerId);
            return;
        }

        function doRender() {
            try {
                if (typeof mermaid === 'undefined') {
                    container.innerHTML = '<div style="color:#c53030;padding:12px;background:#fff5f5;border-radius:6px;">Libreria Mermaid non disponibile.</div>';
                    return;
                }
                mermaid.initialize({
                    startOnLoad: false,
                    theme: 'neutral',
                    maxTextSize: 100000,
                    flowchart: { useMaxWidth: true, htmlLabels: true }
                });

                const uniqueId = 'tree-svg-' + Math.floor(Math.random() * 1000000);
                mermaid.render(uniqueId, mermaidCode).then(({ svg }) => {
                    container.innerHTML = svg;
                    const svgElem = container.querySelector('svg');
                    if (svgElem) {
                        svgElem.style.maxWidth = '100%';
                        svgElem.style.height = 'auto';
                        svgElem.style.maxHeight = '460px';
                        svgElem.style.display = 'block';
                        svgElem.style.margin = '0 auto';
                    }
                }).catch(err => {
                    console.error("TreeViewer render error:", err);
                    container.innerHTML = '<div style="color:#c53030;padding:12px;background:#fff5f5;border:1px solid #feb2b2;border-radius:6px;font-size:13px;"><b>Errore rendering diagramma:</b> ' + err.message + '</div>';
                });
            } catch (e) {
                console.error("TreeViewer exception:", e);
                container.innerHTML = '<div style="color:red;padding:12px;">' + e.message + '</div>';
            }
        }

        if (typeof mermaid === 'undefined') {
            const script = document.createElement('script');
            script.src = '/mermaid.min.js';
            script.onload = () => doRender();
            script.onerror = () => {
                container.innerHTML = '<div style="color:#c53030;padding:12px;">Impossibile caricare /mermaid.min.js</div>';
            };
            document.head.appendChild(script);
        } else {
            doRender();
        }
    }
};
