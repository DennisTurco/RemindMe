import { format } from 'date-fns';


export function formatDate(date: Date | string) {
    if (!date) {
        return '';
    }

    const parsed = typeof date === 'string' ? new Date(date) : date;
    if (Number.isNaN(parsed.getTime())) {
        return '';
    }

    const formattedDate: string = format(parsed, 'dd/MM/yyyy HH:mm');
    return formattedDate; // Output: 01/05/2024 06:10
}
