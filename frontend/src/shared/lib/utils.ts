import { clsx, type ClassValue } from "clsx";
import { twMerge } from "tailwind-merge";

/**
 * Merges Tailwind CSS classes with clsx conditionals and resolves class conflicts
 * 
 * @param inputs - Array of class names, objects, or conditional values
 * @returns Combined and deduplicated class string
 */
export function cn(...inputs: ClassValue[]): string {
  return twMerge(clsx(inputs));
}