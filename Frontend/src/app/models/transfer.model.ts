export type TxStatus = 'ACTIVE' | 'INACTIVE' | 'SUCCESS' | 'FAILED' | 'PENDING' | string;

export interface TransferHistoryItem {
  transactionId?: number | null;
  fromAccountId: string;
  toAccountId: string;
  amount: number;
  status: TxStatus;
  failureReason: string | null;
  createdOn: string;
}

export interface TransferResponseDTO {
  transactionId?: number | null;
  status: string;
  message: string;
}

export function normalizeHistoryItems(data: unknown): TransferHistoryItem[] {
  if (!Array.isArray(data)) {
    return [];
  }

  return data.map((item: any) => ({
    transactionId: item?.transactionId ?? null,
    fromAccountId: item?.fromAccountId ?? '',
    toAccountId: item?.toAccountId ?? '',
    amount: Number(item?.amount ?? 0),
    status: item?.status ?? '',
    failureReason: item?.failureReason ?? null,
    createdOn: item?.createdOn ?? '',
  }));
}
