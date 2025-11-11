import { TestBed } from '@angular/core/testing';
import { EmojiService } from './emoji.service';

describe('EmojiService', () => {
  let service: EmojiService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(EmojiService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should return all emoji categories when no search term', () => {
    const categories = service.getEmojiCategories();
    expect(categories.length).toBeGreaterThan(0);
    expect(categories[0].name).toBeTruthy();
    expect(categories[0].emojis.length).toBeGreaterThan(0);
  });

  it('should search emojis by name', () => {
    const results = service.searchEmojis('smile');
    expect(results.length).toBeGreaterThan(0);

    // Should find emojis with "smile" in their name
    const hasSmileEmoji = results.some(category =>
      category.emojis.some(emoji => emoji.name.toLowerCase().includes('smile'))
    );
    expect(hasSmileEmoji).toBe(true);
  });

  it('should search emojis by keyword', () => {
    const results = service.searchEmojis('happy');
    expect(results.length).toBeGreaterThan(0);

    // Should find emojis with "happy" in their keywords
    const hasHappyKeyword = results.some(category =>
      category.emojis.some(emoji =>
        emoji.keywords.some(keyword => keyword.toLowerCase().includes('happy'))
      )
    );
    expect(hasHappyKeyword).toBe(true);
  });

  it('should search emojis by category name', () => {
    const results = service.searchEmojis('food');
    expect(results.length).toBeGreaterThan(0);

    // Should find the Food category
    const hasFoodCategory = results.some(category =>
      category.name.toLowerCase().includes('food')
    );
    expect(hasFoodCategory).toBe(true);
  });

  it('should return empty array when no matches found', () => {
    const results = service.searchEmojis('xyznonexistent123');
    expect(results.length).toBe(0);
  });

  it('should handle empty search term', () => {
    const results = service.searchEmojis('');
    const allCategories = service.getEmojiCategories();
    expect(results.length).toBe(allCategories.length);
  });

  it('should handle whitespace-only search term', () => {
    const results = service.searchEmojis('   ');
    const allCategories = service.getEmojiCategories();
    expect(results.length).toBe(allCategories.length);
  });

  it('should be case-insensitive', () => {
    const lowerResults = service.searchEmojis('pizza');
    const upperResults = service.searchEmojis('PIZZA');
    const mixedResults = service.searchEmojis('PiZzA');

    expect(lowerResults.length).toBeGreaterThan(0);
    expect(upperResults.length).toBe(lowerResults.length);
    expect(mixedResults.length).toBe(lowerResults.length);
  });

  it('should find heart emojis by keyword', () => {
    const results = service.searchEmojis('love');
    expect(results.length).toBeGreaterThan(0);

    // Should find hearts since they have "love" keyword
    const hasHearts = results.some(category =>
      category.emojis.some(emoji => emoji.emoji.includes('❤') || emoji.emoji.includes('💙') || emoji.emoji.includes('💚'))
    );
    expect(hasHearts).toBe(true);
  });

  it('should find pizza by name', () => {
    const results = service.searchEmojis('pizza');
    expect(results.length).toBeGreaterThan(0);

    const hasPizza = results.some(category =>
      category.emojis.some(emoji => emoji.emoji === '🍕')
    );
    expect(hasPizza).toBe(true);
  });

  it('should maintain emoji metadata in search results', () => {
    const results = service.searchEmojis('smile');

    results.forEach(category => {
      category.emojis.forEach(emoji => {
        expect(emoji.emoji).toBeTruthy();
        expect(emoji.name).toBeTruthy();
        expect(Array.isArray(emoji.keywords)).toBe(true);
        expect(emoji.category).toBeTruthy();
      });
    });
  });

  it('should filter out categories with no matching emojis', () => {
    const results = service.searchEmojis('computer');

    // Should only return categories that have computer-related emojis
    results.forEach(category => {
      expect(category.emojis.length).toBeGreaterThan(0);
    });

    // Computer should be in Objects category
    const hasObjectsCategory = results.some(category =>
      category.name === 'Objects'
    );
    expect(hasObjectsCategory).toBe(true);
  });
});
