import ReactMarkdown from "react-markdown";
import remarkGfm from "remark-gfm";

interface MarkdownContentProps {
  text: string;
  className?: string;
}

/** Renders reminder description text as Markdown (lists, bold, links, ...). */
export function MarkdownContent({ text, className }: MarkdownContentProps) {
  return (
    <div className={`markdown-content ${className ?? ""}`}>
      <ReactMarkdown remarkPlugins={[remarkGfm]}>{text}</ReactMarkdown>
    </div>
  );
}
