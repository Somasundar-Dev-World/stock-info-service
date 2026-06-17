export interface StockResponse {
  symbol: string;
  companyName: string;
  exchange: string;
  currency: string;
  price: number;
  open: number;
  high: number;
  low: number;
  previousClose: number;
  change: number;
  changePercent: string;
  volume: number;
  marketCap: string;
  peRatio: string;
  week52High: number;
  week52Low: number;
  latestTradingDay: string;
  dataSource: string;
}

export interface ErrorResponse {
  status: number;
  error: string;
  message: string;
  path: string;
  timestamp: string;
  validationErrors?: { [key: string]: string };
}
