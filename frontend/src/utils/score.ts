import { formatDuration, intervalToDuration } from "date-fns";
import { Player } from "../types";

export function formatSurvivalTime(ms: number) {
  return formatDuration(intervalToDuration({ start: 0, end: Math.round(ms) }));
}

export function sortPlayers(players: Player[]) {
  return players
    .slice(0)
    .sort((p1, p2) => p2.playerStats.score - p1.playerStats.score);
}
