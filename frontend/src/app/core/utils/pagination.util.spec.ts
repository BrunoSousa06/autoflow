import { normalizePage } from './pagination.util';

describe('normalizePage', () => {
  it('normalizes a Page-like object with page metadata', () => {
    const raw = {
      content: [{ id: 1 }, { id: 2 }],
      page: { totalElements: 42, number: 2, size: 10 },
    };

    const p = normalizePage(raw, 20);
    expect(p.content.length).toBe(2);
    expect(p.totalElements).toBe(42);
    expect(p.pageNumber).toBe(2);
    expect(p.pageSize).toBe(10);
  });

  it('normalizes when backend returns array directly', () => {
    const raw = [{ id: 'a' }, { id: 'b' }];
    const p = normalizePage(raw, 5);
    expect(p.content.length).toBe(2);
    expect(p.totalElements).toBe(2);
    expect(p.pageNumber).toBe(0);
    expect(p.pageSize).toBe(5);
  });

  it('reads top-level totalElements and pageNumber/pageSize when present', () => {
    const raw = {
      content: [{}, {}],
      totalElements: 7,
      pageNumber: 1,
      size: 3,
    };
    const p = normalizePage(raw, 10);
    expect(p.totalElements).toBe(7);
    expect(p.pageNumber).toBe(1);
    expect(p.pageSize).toBe(3);
  });

  it('falls back to defaults when values are missing or invalid', () => {
    const raw = { content: 'not-an-array' } as any;
    const p = normalizePage<any>(raw, 9);
    expect(Array.isArray(p.content)).toBe(true);
    expect(p.content.length).toBe(0);
    expect(p.totalElements).toBe(0);
    expect(p.pageNumber).toBe(0);
    expect(p.pageSize).toBe(9);
  });
});
