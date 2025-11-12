import { Injectable } from '@angular/core';

export interface EmojiData {
  emoji: string;
  name: string;
  keywords: string[];
  category: string;
}

export interface EmojiCategory {
  name: string;
  emojis: EmojiData[];
}

@Injectable({
  providedIn: 'root'
})
export class EmojiService {
  private emojiData: EmojiCategory[] = [
    {
      name: 'Smileys',
      emojis: [
        { emoji: '😀', name: 'grinning face', keywords: ['smile', 'happy', 'joy', 'grin'], category: 'Smileys' },
        { emoji: '😃', name: 'grinning face with big eyes', keywords: ['smile', 'happy', 'joy'], category: 'Smileys' },
        { emoji: '😄', name: 'grinning face with smiling eyes', keywords: ['smile', 'happy', 'joy', 'laugh'], category: 'Smileys' },
        { emoji: '😁', name: 'beaming face with smiling eyes', keywords: ['smile', 'happy', 'grin'], category: 'Smileys' },
        { emoji: '😅', name: 'grinning face with sweat', keywords: ['smile', 'sweat', 'relief'], category: 'Smileys' },
        { emoji: '😂', name: 'face with tears of joy', keywords: ['laugh', 'cry', 'tears', 'lol'], category: 'Smileys' },
        { emoji: '🤣', name: 'rolling on the floor laughing', keywords: ['laugh', 'lol', 'rofl'], category: 'Smileys' },
        { emoji: '😊', name: 'smiling face with smiling eyes', keywords: ['smile', 'happy', 'blush'], category: 'Smileys' },
        { emoji: '😇', name: 'smiling face with halo', keywords: ['angel', 'innocent', 'good'], category: 'Smileys' },
        { emoji: '🙂', name: 'slightly smiling face', keywords: ['smile', 'happy'], category: 'Smileys' },
        { emoji: '🙃', name: 'upside-down face', keywords: ['silly', 'sarcasm', 'irony'], category: 'Smileys' },
        { emoji: '😉', name: 'winking face', keywords: ['wink', 'flirt'], category: 'Smileys' },
        { emoji: '😌', name: 'relieved face', keywords: ['relief', 'calm', 'peace'], category: 'Smileys' },
        { emoji: '😍', name: 'smiling face with heart-eyes', keywords: ['love', 'heart', 'like', 'crush'], category: 'Smileys' },
        { emoji: '🥰', name: 'smiling face with hearts', keywords: ['love', 'heart', 'adore'], category: 'Smileys' },
        { emoji: '😘', name: 'face blowing a kiss', keywords: ['kiss', 'love', 'heart'], category: 'Smileys' },
        { emoji: '😋', name: 'face savoring food', keywords: ['yum', 'delicious', 'food', 'tasty'], category: 'Smileys' },
        { emoji: '😎', name: 'smiling face with sunglasses', keywords: ['cool', 'sunglasses', 'confident'], category: 'Smileys' },
        { emoji: '🥳', name: 'partying face', keywords: ['party', 'celebrate', 'birthday'], category: 'Smileys' },
        { emoji: '😏', name: 'smirking face', keywords: ['smirk', 'smug'], category: 'Smileys' },
        { emoji: '😞', name: 'disappointed face', keywords: ['sad', 'disappointed', 'unhappy'], category: 'Smileys' },
        { emoji: '😔', name: 'pensive face', keywords: ['sad', 'thoughtful', 'pensive'], category: 'Smileys' },
        { emoji: '😟', name: 'worried face', keywords: ['worried', 'concerned', 'anxious'], category: 'Smileys' },
        { emoji: '😕', name: 'confused face', keywords: ['confused', 'puzzled'], category: 'Smileys' },
        { emoji: '😢', name: 'crying face', keywords: ['cry', 'sad', 'tears'], category: 'Smileys' },
        { emoji: '😭', name: 'loudly crying face', keywords: ['cry', 'sob', 'tears', 'sad'], category: 'Smileys' },
        { emoji: '😤', name: 'face with steam from nose', keywords: ['angry', 'frustrated', 'mad'], category: 'Smileys' },
        { emoji: '😠', name: 'angry face', keywords: ['angry', 'mad', 'annoyed'], category: 'Smileys' },
        { emoji: '😡', name: 'pouting face', keywords: ['angry', 'mad', 'rage'], category: 'Smileys' },
        { emoji: '🤬', name: 'face with symbols on mouth', keywords: ['angry', 'curse', 'swear'], category: 'Smileys' },
        { emoji: '😱', name: 'face screaming in fear', keywords: ['scream', 'scared', 'shocked'], category: 'Smileys' },
        { emoji: '😴', name: 'sleeping face', keywords: ['sleep', 'tired', 'zzz'], category: 'Smileys' },
        { emoji: '🤤', name: 'drooling face', keywords: ['drool', 'hungry'], category: 'Smileys' },
        { emoji: '🤔', name: 'thinking face', keywords: ['think', 'wonder', 'hmm'], category: 'Smileys' },
        { emoji: '🤐', name: 'zipper-mouth face', keywords: ['quiet', 'secret', 'silence'], category: 'Smileys' },
        { emoji: '🤢', name: 'nauseated face', keywords: ['sick', 'ill', 'nausea'], category: 'Smileys' },
        { emoji: '🤮', name: 'face vomiting', keywords: ['sick', 'vomit', 'ill'], category: 'Smileys' }
      ]
    },
    {
      name: 'Food',
      emojis: [
        { emoji: '🍕', name: 'pizza', keywords: ['pizza', 'food', 'italian', 'slice'], category: 'Food' },
        { emoji: '🍔', name: 'hamburger', keywords: ['burger', 'food', 'fast food'], category: 'Food' },
        { emoji: '🍟', name: 'french fries', keywords: ['fries', 'food', 'potato', 'fast food'], category: 'Food' },
        { emoji: '🌭', name: 'hot dog', keywords: ['hotdog', 'food', 'sausage'], category: 'Food' },
        { emoji: '🍿', name: 'popcorn', keywords: ['popcorn', 'snack', 'movie'], category: 'Food' },
        { emoji: '🥐', name: 'croissant', keywords: ['croissant', 'bread', 'breakfast', 'french'], category: 'Food' },
        { emoji: '🥖', name: 'baguette bread', keywords: ['bread', 'baguette', 'french'], category: 'Food' },
        { emoji: '🍞', name: 'bread', keywords: ['bread', 'loaf', 'toast'], category: 'Food' },
        { emoji: '🧀', name: 'cheese wedge', keywords: ['cheese', 'dairy'], category: 'Food' },
        { emoji: '🍖', name: 'meat on bone', keywords: ['meat', 'bone', 'food'], category: 'Food' },
        { emoji: '🍗', name: 'poultry leg', keywords: ['chicken', 'meat', 'food', 'drumstick'], category: 'Food' },
        { emoji: '🥩', name: 'cut of meat', keywords: ['steak', 'meat', 'beef'], category: 'Food' },
        { emoji: '🥓', name: 'bacon', keywords: ['bacon', 'meat', 'breakfast'], category: 'Food' },
        { emoji: '🌮', name: 'taco', keywords: ['taco', 'mexican', 'food'], category: 'Food' },
        { emoji: '🌯', name: 'burrito', keywords: ['burrito', 'mexican', 'food', 'wrap'], category: 'Food' },
        { emoji: '🥙', name: 'stuffed flatbread', keywords: ['flatbread', 'kebab', 'gyro'], category: 'Food' },
        { emoji: '🥚', name: 'egg', keywords: ['egg', 'breakfast', 'food'], category: 'Food' },
        { emoji: '🍳', name: 'cooking', keywords: ['fried egg', 'cooking', 'breakfast'], category: 'Food' },
        { emoji: '🥘', name: 'shallow pan of food', keywords: ['paella', 'food', 'pan'], category: 'Food' },
        { emoji: '🍲', name: 'pot of food', keywords: ['stew', 'soup', 'pot'], category: 'Food' },
        { emoji: '🥗', name: 'green salad', keywords: ['salad', 'healthy', 'vegetables'], category: 'Food' },
        { emoji: '🍜', name: 'steaming bowl', keywords: ['ramen', 'noodles', 'soup'], category: 'Food' },
        { emoji: '🍝', name: 'spaghetti', keywords: ['pasta', 'spaghetti', 'italian'], category: 'Food' },
        { emoji: '🍣', name: 'sushi', keywords: ['sushi', 'japanese', 'fish'], category: 'Food' },
        { emoji: '🍱', name: 'bento box', keywords: ['bento', 'japanese', 'lunch'], category: 'Food' },
        { emoji: '🍦', name: 'soft ice cream', keywords: ['ice cream', 'soft serve', 'dessert'], category: 'Food' },
        { emoji: '🍨', name: 'ice cream', keywords: ['ice cream', 'dessert', 'sweet'], category: 'Food' },
        { emoji: '🍩', name: 'doughnut', keywords: ['donut', 'doughnut', 'dessert', 'sweet'], category: 'Food' },
        { emoji: '🍪', name: 'cookie', keywords: ['cookie', 'dessert', 'sweet', 'biscuit'], category: 'Food' },
        { emoji: '🎂', name: 'birthday cake', keywords: ['cake', 'birthday', 'dessert', 'celebration'], category: 'Food' },
        { emoji: '🍰', name: 'shortcake', keywords: ['cake', 'dessert', 'sweet', 'slice'], category: 'Food' },
        { emoji: '🧁', name: 'cupcake', keywords: ['cupcake', 'dessert', 'sweet'], category: 'Food' },
        { emoji: '🍫', name: 'chocolate bar', keywords: ['chocolate', 'sweet', 'candy'], category: 'Food' },
        { emoji: '🍬', name: 'candy', keywords: ['candy', 'sweet'], category: 'Food' },
        { emoji: '☕', name: 'hot beverage', keywords: ['coffee', 'tea', 'hot', 'drink'], category: 'Food' },
        { emoji: '🍵', name: 'teacup without handle', keywords: ['tea', 'drink', 'green tea'], category: 'Food' },
        { emoji: '🥤', name: 'cup with straw', keywords: ['drink', 'soda', 'beverage'], category: 'Food' }
      ]
    },
    {
      name: 'Activities',
      emojis: [
        { emoji: '⚽', name: 'soccer ball', keywords: ['soccer', 'football', 'sports', 'ball'], category: 'Activities' },
        { emoji: '🏀', name: 'basketball', keywords: ['basketball', 'sports', 'ball'], category: 'Activities' },
        { emoji: '🏈', name: 'american football', keywords: ['football', 'sports', 'ball'], category: 'Activities' },
        { emoji: '⚾', name: 'baseball', keywords: ['baseball', 'sports', 'ball'], category: 'Activities' },
        { emoji: '🎾', name: 'tennis', keywords: ['tennis', 'sports', 'ball'], category: 'Activities' },
        { emoji: '🏐', name: 'volleyball', keywords: ['volleyball', 'sports', 'ball'], category: 'Activities' },
        { emoji: '🎮', name: 'video game', keywords: ['game', 'gaming', 'controller', 'video game'], category: 'Activities' },
        { emoji: '🎯', name: 'direct hit', keywords: ['target', 'bullseye', 'dart'], category: 'Activities' },
        { emoji: '🎲', name: 'game die', keywords: ['dice', 'game', 'random'], category: 'Activities' },
        { emoji: '🎭', name: 'performing arts', keywords: ['theater', 'drama', 'masks'], category: 'Activities' },
        { emoji: '🎨', name: 'artist palette', keywords: ['art', 'paint', 'painting', 'creative'], category: 'Activities' },
        { emoji: '🎬', name: 'clapper board', keywords: ['movie', 'film', 'cinema', 'action'], category: 'Activities' },
        { emoji: '🎤', name: 'microphone', keywords: ['mic', 'sing', 'music', 'karaoke'], category: 'Activities' },
        { emoji: '🎧', name: 'headphone', keywords: ['headphones', 'music', 'listen'], category: 'Activities' },
        { emoji: '🎸', name: 'guitar', keywords: ['guitar', 'music', 'rock'], category: 'Activities' },
        { emoji: '🎹', name: 'musical keyboard', keywords: ['piano', 'keyboard', 'music'], category: 'Activities' },
        { emoji: '🥁', name: 'drum', keywords: ['drum', 'music', 'percussion'], category: 'Activities' }
      ]
    },
    {
      name: 'Nature',
      emojis: [
        { emoji: '🐶', name: 'dog face', keywords: ['dog', 'puppy', 'pet', 'animal'], category: 'Nature' },
        { emoji: '🐱', name: 'cat face', keywords: ['cat', 'kitten', 'pet', 'animal'], category: 'Nature' },
        { emoji: '🐭', name: 'mouse face', keywords: ['mouse', 'animal'], category: 'Nature' },
        { emoji: '🐹', name: 'hamster', keywords: ['hamster', 'pet', 'animal'], category: 'Nature' },
        { emoji: '🐰', name: 'rabbit face', keywords: ['rabbit', 'bunny', 'animal'], category: 'Nature' },
        { emoji: '🦊', name: 'fox', keywords: ['fox', 'animal'], category: 'Nature' },
        { emoji: '🐻', name: 'bear', keywords: ['bear', 'animal'], category: 'Nature' },
        { emoji: '🐼', name: 'panda', keywords: ['panda', 'bear', 'animal'], category: 'Nature' },
        { emoji: '🐨', name: 'koala', keywords: ['koala', 'animal', 'australia'], category: 'Nature' },
        { emoji: '🐯', name: 'tiger face', keywords: ['tiger', 'animal', 'big cat'], category: 'Nature' },
        { emoji: '🦁', name: 'lion', keywords: ['lion', 'animal', 'big cat'], category: 'Nature' },
        { emoji: '🐮', name: 'cow face', keywords: ['cow', 'animal', 'cattle'], category: 'Nature' },
        { emoji: '🐷', name: 'pig face', keywords: ['pig', 'animal'], category: 'Nature' },
        { emoji: '🐸', name: 'frog', keywords: ['frog', 'animal'], category: 'Nature' },
        { emoji: '🐵', name: 'monkey face', keywords: ['monkey', 'animal'], category: 'Nature' },
        { emoji: '🐔', name: 'chicken', keywords: ['chicken', 'bird', 'animal'], category: 'Nature' },
        { emoji: '🐧', name: 'penguin', keywords: ['penguin', 'bird', 'animal'], category: 'Nature' },
        { emoji: '🦅', name: 'eagle', keywords: ['eagle', 'bird', 'animal'], category: 'Nature' },
        { emoji: '🦉', name: 'owl', keywords: ['owl', 'bird', 'animal', 'wise'], category: 'Nature' },
        { emoji: '🦋', name: 'butterfly', keywords: ['butterfly', 'insect', 'beautiful'], category: 'Nature' },
        { emoji: '🐝', name: 'honeybee', keywords: ['bee', 'insect', 'honey'], category: 'Nature' },
        { emoji: '🐢', name: 'turtle', keywords: ['turtle', 'animal', 'slow'], category: 'Nature' },
        { emoji: '🐍', name: 'snake', keywords: ['snake', 'animal', 'reptile'], category: 'Nature' },
        { emoji: '🐙', name: 'octopus', keywords: ['octopus', 'animal', 'sea'], category: 'Nature' },
        { emoji: '🐠', name: 'tropical fish', keywords: ['fish', 'tropical', 'animal'], category: 'Nature' },
        { emoji: '🐬', name: 'dolphin', keywords: ['dolphin', 'animal', 'sea'], category: 'Nature' },
        { emoji: '🦈', name: 'shark', keywords: ['shark', 'animal', 'sea'], category: 'Nature' },
        { emoji: '🌲', name: 'evergreen tree', keywords: ['tree', 'pine', 'nature'], category: 'Nature' },
        { emoji: '🌳', name: 'deciduous tree', keywords: ['tree', 'nature'], category: 'Nature' },
        { emoji: '🌴', name: 'palm tree', keywords: ['palm', 'tree', 'tropical'], category: 'Nature' },
        { emoji: '🌱', name: 'seedling', keywords: ['plant', 'seedling', 'grow'], category: 'Nature' },
        { emoji: '🌿', name: 'herb', keywords: ['herb', 'plant', 'leaf'], category: 'Nature' },
        { emoji: '🍀', name: 'four leaf clover', keywords: ['clover', 'lucky', 'four leaf'], category: 'Nature' },
        { emoji: '🌻', name: 'sunflower', keywords: ['sunflower', 'flower', 'sun'], category: 'Nature' },
        { emoji: '🌹', name: 'rose', keywords: ['rose', 'flower', 'love'], category: 'Nature' },
        { emoji: '🌺', name: 'hibiscus', keywords: ['hibiscus', 'flower', 'tropical'], category: 'Nature' },
        { emoji: '🌸', name: 'cherry blossom', keywords: ['cherry blossom', 'flower', 'spring'], category: 'Nature' },
        { emoji: '🌼', name: 'blossom', keywords: ['flower', 'blossom', 'yellow'], category: 'Nature' },
        { emoji: '🌞', name: 'sun with face', keywords: ['sun', 'sunny', 'bright'], category: 'Nature' },
        { emoji: '🌙', name: 'crescent moon', keywords: ['moon', 'night'], category: 'Nature' },
        { emoji: '⭐', name: 'star', keywords: ['star', 'favorite'], category: 'Nature' },
        { emoji: '🌟', name: 'glowing star', keywords: ['star', 'shine', 'sparkle'], category: 'Nature' },
        { emoji: '⚡', name: 'high voltage', keywords: ['lightning', 'electric', 'bolt', 'energy'], category: 'Nature' },
        { emoji: '🔥', name: 'fire', keywords: ['fire', 'flame', 'hot'], category: 'Nature' },
        { emoji: '🌈', name: 'rainbow', keywords: ['rainbow', 'colorful'], category: 'Nature' },
        { emoji: '☀️', name: 'sun', keywords: ['sun', 'sunny', 'weather'], category: 'Nature' },
        { emoji: '⛅', name: 'sun behind cloud', keywords: ['cloud', 'sun', 'weather'], category: 'Nature' },
        { emoji: '☁️', name: 'cloud', keywords: ['cloud', 'weather'], category: 'Nature' },
        { emoji: '🌧️', name: 'cloud with rain', keywords: ['rain', 'weather', 'cloud'], category: 'Nature' },
        { emoji: '⛈️', name: 'cloud with lightning and rain', keywords: ['storm', 'thunder', 'weather'], category: 'Nature' },
        { emoji: '❄️', name: 'snowflake', keywords: ['snow', 'winter', 'cold'], category: 'Nature' },
        { emoji: '☃️', name: 'snowman', keywords: ['snowman', 'winter', 'snow'], category: 'Nature' },
        { emoji: '🌊', name: 'water wave', keywords: ['wave', 'water', 'ocean', 'sea'], category: 'Nature' }
      ]
    },
    {
      name: 'Objects',
      emojis: [
        { emoji: '📱', name: 'mobile phone', keywords: ['phone', 'mobile', 'smartphone'], category: 'Objects' },
        { emoji: '💻', name: 'laptop', keywords: ['computer', 'laptop', 'pc'], category: 'Objects' },
        { emoji: '⌨️', name: 'keyboard', keywords: ['keyboard', 'type'], category: 'Objects' },
        { emoji: '🖥️', name: 'desktop computer', keywords: ['computer', 'desktop', 'pc'], category: 'Objects' },
        { emoji: '🖨️', name: 'printer', keywords: ['printer', 'print'], category: 'Objects' },
        { emoji: '🖱️', name: 'computer mouse', keywords: ['mouse', 'computer'], category: 'Objects' },
        { emoji: '📷', name: 'camera', keywords: ['camera', 'photo', 'picture'], category: 'Objects' },
        { emoji: '📸', name: 'camera with flash', keywords: ['camera', 'photo', 'flash'], category: 'Objects' },
        { emoji: '📺', name: 'television', keywords: ['tv', 'television', 'watch'], category: 'Objects' },
        { emoji: '📻', name: 'radio', keywords: ['radio', 'music', 'listen'], category: 'Objects' },
        { emoji: '⏰', name: 'alarm clock', keywords: ['clock', 'alarm', 'time', 'wake'], category: 'Objects' },
        { emoji: '⌚', name: 'watch', keywords: ['watch', 'time'], category: 'Objects' },
        { emoji: '💡', name: 'light bulb', keywords: ['light', 'bulb', 'idea'], category: 'Objects' },
        { emoji: '🔦', name: 'flashlight', keywords: ['flashlight', 'torch', 'light'], category: 'Objects' },
        { emoji: '🕯️', name: 'candle', keywords: ['candle', 'light', 'flame'], category: 'Objects' },
        { emoji: '💰', name: 'money bag', keywords: ['money', 'dollar', 'rich', 'cash'], category: 'Objects' },
        { emoji: '💳', name: 'credit card', keywords: ['credit card', 'payment', 'money'], category: 'Objects' },
        { emoji: '💎', name: 'gem stone', keywords: ['diamond', 'gem', 'jewel'], category: 'Objects' },
        { emoji: '🔧', name: 'wrench', keywords: ['wrench', 'tool', 'fix'], category: 'Objects' },
        { emoji: '🔨', name: 'hammer', keywords: ['hammer', 'tool', 'build'], category: 'Objects' },
        { emoji: '🔑', name: 'key', keywords: ['key', 'unlock', 'lock'], category: 'Objects' },
        { emoji: '🚪', name: 'door', keywords: ['door', 'entry', 'exit'], category: 'Objects' },
        { emoji: '🎁', name: 'wrapped gift', keywords: ['gift', 'present', 'birthday'], category: 'Objects' },
        { emoji: '🎈', name: 'balloon', keywords: ['balloon', 'party', 'celebrate'], category: 'Objects' },
        { emoji: '🎉', name: 'party popper', keywords: ['party', 'celebrate', 'confetti'], category: 'Objects' },
        { emoji: '✉️', name: 'envelope', keywords: ['mail', 'email', 'letter'], category: 'Objects' },
        { emoji: '📧', name: 'e-mail', keywords: ['email', 'mail', 'message'], category: 'Objects' },
        { emoji: '📚', name: 'books', keywords: ['book', 'books', 'read', 'library'], category: 'Objects' },
        { emoji: '📖', name: 'open book', keywords: ['book', 'read', 'open'], category: 'Objects' },
        { emoji: '📝', name: 'memo', keywords: ['memo', 'note', 'write', 'pencil'], category: 'Objects' },
        { emoji: '✏️', name: 'pencil', keywords: ['pencil', 'write', 'draw'], category: 'Objects' },
        { emoji: '🔍', name: 'magnifying glass tilted left', keywords: ['search', 'find', 'magnify'], category: 'Objects' },
        { emoji: '🔎', name: 'magnifying glass tilted right', keywords: ['search', 'find', 'magnify'], category: 'Objects' },
        { emoji: '🔒', name: 'locked', keywords: ['lock', 'locked', 'secure', 'private'], category: 'Objects' },
        { emoji: '🔓', name: 'unlocked', keywords: ['unlock', 'unlocked', 'open'], category: 'Objects' }
      ]
    },
    {
      name: 'Symbols',
      emojis: [
        { emoji: '❤️', name: 'red heart', keywords: ['heart', 'love', 'like', 'red'], category: 'Symbols' },
        { emoji: '🧡', name: 'orange heart', keywords: ['heart', 'love', 'orange'], category: 'Symbols' },
        { emoji: '💛', name: 'yellow heart', keywords: ['heart', 'love', 'yellow'], category: 'Symbols' },
        { emoji: '💚', name: 'green heart', keywords: ['heart', 'love', 'green'], category: 'Symbols' },
        { emoji: '💙', name: 'blue heart', keywords: ['heart', 'love', 'blue'], category: 'Symbols' },
        { emoji: '💜', name: 'purple heart', keywords: ['heart', 'love', 'purple'], category: 'Symbols' },
        { emoji: '🖤', name: 'black heart', keywords: ['heart', 'love', 'black'], category: 'Symbols' },
        { emoji: '🤍', name: 'white heart', keywords: ['heart', 'love', 'white'], category: 'Symbols' },
        { emoji: '💔', name: 'broken heart', keywords: ['heart', 'broken', 'sad', 'heartbreak'], category: 'Symbols' },
        { emoji: '✅', name: 'check mark button', keywords: ['check', 'yes', 'done', 'correct'], category: 'Symbols' },
        { emoji: '❌', name: 'cross mark', keywords: ['x', 'cross', 'no', 'wrong', 'cancel'], category: 'Symbols' },
        { emoji: '⭕', name: 'hollow red circle', keywords: ['circle', 'o', 'ring'], category: 'Symbols' },
        { emoji: '❗', name: 'exclamation mark', keywords: ['exclamation', 'warning', 'important'], category: 'Symbols' },
        { emoji: '❓', name: 'question mark', keywords: ['question', 'help', 'confused'], category: 'Symbols' },
        { emoji: '⚠️', name: 'warning', keywords: ['warning', 'caution', 'alert'], category: 'Symbols' },
        { emoji: '♻️', name: 'recycling symbol', keywords: ['recycle', 'environment', 'green'], category: 'Symbols' },
        { emoji: '⚡', name: 'high voltage', keywords: ['lightning', 'electric', 'fast', 'energy'], category: 'Symbols' },
        { emoji: '🔥', name: 'fire', keywords: ['fire', 'hot', 'flame'], category: 'Symbols' },
        { emoji: '💯', name: 'hundred points', keywords: ['100', 'hundred', 'perfect', 'score'], category: 'Symbols' },
        { emoji: '🎵', name: 'musical note', keywords: ['music', 'note', 'song'], category: 'Symbols' },
        { emoji: '🎶', name: 'musical notes', keywords: ['music', 'notes', 'song'], category: 'Symbols' },
        { emoji: '➕', name: 'plus', keywords: ['plus', 'add', 'more'], category: 'Symbols' },
        { emoji: '➖', name: 'minus', keywords: ['minus', 'subtract', 'less'], category: 'Symbols' },
        { emoji: '✖️', name: 'multiplication', keywords: ['multiply', 'times', 'x'], category: 'Symbols' },
        { emoji: '➗', name: 'division', keywords: ['divide', 'division'], category: 'Symbols' },
        { emoji: '™️', name: 'trade mark', keywords: ['trademark', 'tm'], category: 'Symbols' },
        { emoji: '©️', name: 'copyright', keywords: ['copyright', 'c'], category: 'Symbols' },
        { emoji: '®️', name: 'registered', keywords: ['registered', 'r'], category: 'Symbols' },
        { emoji: '✔️', name: 'check mark', keywords: ['check', 'yes', 'done', 'tick'], category: 'Symbols' },
        { emoji: '☑️', name: 'check box with check', keywords: ['checkbox', 'check', 'yes', 'done'], category: 'Symbols' },
        { emoji: '🔴', name: 'red circle', keywords: ['red', 'circle', 'dot'], category: 'Symbols' },
        { emoji: '🟠', name: 'orange circle', keywords: ['orange', 'circle', 'dot'], category: 'Symbols' },
        { emoji: '🟡', name: 'yellow circle', keywords: ['yellow', 'circle', 'dot'], category: 'Symbols' },
        { emoji: '🟢', name: 'green circle', keywords: ['green', 'circle', 'dot'], category: 'Symbols' },
        { emoji: '🔵', name: 'blue circle', keywords: ['blue', 'circle', 'dot'], category: 'Symbols' },
        { emoji: '🟣', name: 'purple circle', keywords: ['purple', 'circle', 'dot'], category: 'Symbols' },
        { emoji: '⚫', name: 'black circle', keywords: ['black', 'circle', 'dot'], category: 'Symbols' },
        { emoji: '⚪', name: 'white circle', keywords: ['white', 'circle', 'dot'], category: 'Symbols' }
      ]
    }
  ];

  getEmojiCategories(): EmojiCategory[] {
    return this.emojiData;
  }

  searchEmojis(searchTerm: string): EmojiCategory[] {
    if (!searchTerm || !searchTerm.trim()) {
      return this.emojiData;
    }

    const lowerSearchTerm = searchTerm.toLowerCase().trim();

    return this.emojiData
      .map(category => ({
        ...category,
        emojis: category.emojis.filter(emojiData =>
          // Search in name
          emojiData.name.toLowerCase().includes(lowerSearchTerm) ||
          // Search in keywords
          emojiData.keywords.some(keyword => keyword.toLowerCase().includes(lowerSearchTerm)) ||
          // Search in category name
          category.name.toLowerCase().includes(lowerSearchTerm)
        )
      }))
      .filter(category => category.emojis.length > 0);
  }
}
