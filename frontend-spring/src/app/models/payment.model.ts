export interface Payment {
  id: string;
  enrollmentId: string;
  parentId: string;
  nurseryId: string;
  amount: number;
  status: string;
  dueDate: string;
  paidAt?: string;
  childName?: string;
  nurseryName?: string;
  parentName?: string;
}

export interface FinancialStats {
  totalRevenue: number;
  pendingPayments: number;
  completedPayments: number;
  monthlyRevenue: { month: string; revenue: number }[];
}
