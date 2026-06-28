export interface NormalizedPage<T> {
  content: T[];
  totalElements: number;
  pageNumber: number;
  pageSize: number;
}

export function normalizePage<T>(raw: any, defaultPageSize = 20): NormalizedPage<T> {
  const content = (raw && (raw.content ?? raw)) || [];
  const arr = Array.isArray(content) ? content as T[] : [];

  const totalFromPage = raw?.page?.totalElements ?? raw?.totalElements;
  const total = typeof totalFromPage === 'number' ? totalFromPage : arr.length;

  const pageNumber = raw?.page?.number ?? raw?.pageNumber ?? 0;
  const pageSize = raw?.page?.size ?? raw?.size ?? defaultPageSize;

  return {
    content: arr,
    totalElements: typeof total === 'number' ? total : arr.length,
    pageNumber: typeof pageNumber === 'number' ? pageNumber : 0,
    pageSize: typeof pageSize === 'number' ? pageSize : defaultPageSize,
  };
}
