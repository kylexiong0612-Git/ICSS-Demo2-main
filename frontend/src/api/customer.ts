import request from '@/utils/request'
import type { Customer } from '@/types'

interface BackendCustomer extends Omit<Customer, 'level'> {
  tier: '普通' | '银卡' | '金卡' | '白金'
}

export function getCustomer(customerId: string) {
  const mapTierToLevel = (
    tier: BackendCustomer['tier'],
  ): Customer['level'] => {
    if (tier === '银卡') return 'Silver'
    if (tier === '金卡') return 'Gold'
    if (tier === '白金') return 'VIP'
    return 'Normal'
  }

  return request.get<unknown, BackendCustomer>(`/api/customers/${customerId}`).then(data => ({
    ...data,
    level: mapTierToLevel(data.tier),
  }))
}
