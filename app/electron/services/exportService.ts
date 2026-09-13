import * as fs from "fs";
import PDFDocument from "pdfkit";
import type { ExecutionMethod, Remind } from "../types";

/** Mirrors Remind#getCSVHeader / Remind#toArrayString in the Java app. */
export const CSV_HEADER = "Name,Active,TopLevel,LastExecution,NextExecution,Interval (gg.HH:mm),ExecutionMethod,TimeFrom,TimeTo";

const EXECUTION_METHOD_NAMES: Record<ExecutionMethod, string> = {
  PC_STARTUP: "Pc Startup",
  CUSTOM_TIME_RANGE: "Custom Time Range",
  ONE_TIME_PER_DAY: "One Time Per Day",
};

export function remindToRowValues(remind: Remind): string[] {
  return [
    remind.name,
    String(remind.isActive),
    String(remind.isTopLevel),
    remind.lastExecution ?? "",
    remind.nextExecution ?? "",
    remind.timeInterval ? `${remind.timeInterval.days}.${remind.timeInterval.hours}:${remind.timeInterval.minutes}` : "",
    EXECUTION_METHOD_NAMES[remind.executionMethod],
    remind.timeRange?.start ?? "",
    remind.timeRange?.end ?? "",
  ];
}

function escapeCsv(value: string): string {
  if (value.includes(",") || value.includes('"') || value.includes("\n")) {
    return `"${value.replace(/"/g, '""')}"`;
  }
  return value;
}

export function exportToCsv(reminds: Remind[], filePath: string): void {
  const lines = [CSV_HEADER, ...reminds.map((r) => remindToRowValues(r).map(escapeCsv).join(","))];
  fs.writeFileSync(filePath, lines.join("\n") + "\n", "utf-8");
}

function wrapText(text: string, max: number): string {
  const chunks: string[] = [];
  for (let i = 0; i < text.length; i += max) {
    chunks.push(text.slice(i, i + max));
  }
  return chunks.join("\n");
}

/**
 * Mirrors ImportExportManager#exportRemindListAsPDF: a title followed by a
 * grid table (header row + one row per reminder), drawn manually since
 * pdfkit has no built-in table support.
 */
export function exportToPdf(reminds: Remind[], filePath: string, title: string): Promise<void> {
  return new Promise((resolve, reject) => {
    const doc = new PDFDocument({ margin: 30, layout: "landscape", size: "A4" });
    const stream = fs.createWriteStream(filePath);
    doc.pipe(stream);

    doc.fontSize(12).font("Helvetica-Bold").text(title);
    doc.moveDown(0.5);

    const headers = CSV_HEADER.split(",");
    const pageWidth = doc.page.width - doc.page.margins.left - doc.page.margins.right;
    const colWidth = pageWidth / headers.length;
    const rowHeight = 22;

    function drawRow(values: string[], y: number, bold: boolean): void {
      doc.font(bold ? "Helvetica-Bold" : "Helvetica").fontSize(bold ? 8 : 5);
      values.forEach((value, i) => {
        const x = doc.page.margins.left + i * colWidth;
        doc.rect(x, y, colWidth, rowHeight).stroke();
        doc.text(wrapText(value.trim(), 25), x + 2, y + 2, { width: colWidth - 4, height: rowHeight - 4 });
      });
    }

    let y = doc.y;
    drawRow(headers, y, true);
    y += rowHeight;

    for (const remind of reminds) {
      if (y + rowHeight > doc.page.height - doc.page.margins.bottom) {
        doc.addPage();
        y = doc.page.margins.top;
      }
      drawRow(remindToRowValues(remind), y, false);
      y += rowHeight;
    }

    doc.end();
    stream.on("finish", () => resolve());
    stream.on("error", reject);
  });
}
