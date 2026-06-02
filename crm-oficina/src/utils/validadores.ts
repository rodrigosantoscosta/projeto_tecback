export function validarCpf(cpf: string): boolean {
  const digits = cpf.replace(/\D/g, '')
  if (digits.length !== 11 || /^(\d)\1+$/.test(digits)) return false
  let sum = 0
  for (let i = 0; i < 9; i++) sum += Number(digits[i]) * (10 - i)
  let r = (sum * 10) % 11
  if (r === 10 || r === 11) r = 0
  if (r !== Number(digits[9])) return false
  sum = 0
  for (let i = 0; i < 10; i++) sum += Number(digits[i]) * (11 - i)
  r = (sum * 10) % 11
  if (r === 10 || r === 11) r = 0
  return r === Number(digits[10])
}

export function validarCnpj(cnpj: string): boolean {
  const digits = cnpj.replace(/\D/g, '')
  if (digits.length !== 14 || /^(\d)\1+$/.test(digits)) return false
  const calc = (d: string, weights: number[]) => {
    const sum = weights.reduce((acc, w, i) => acc + Number(d[i]) * w, 0)
    const r = sum % 11
    return r < 2 ? 0 : 11 - r
  }
  const w1 = [5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2]
  const w2 = [6, 5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2]
  return calc(digits, w1) === Number(digits[12]) && calc(digits, w2) === Number(digits[13])
}

export function validarCpfOuCnpj(value: string): boolean {
  const digits = value.replace(/\D/g, '')
  if (digits.length === 11) return validarCpf(digits)
  if (digits.length === 14) return validarCnpj(digits)
  return false
}

export const PLACA_MERCOSUL_REGEX = /^[A-Z]{3}[0-9][A-Z0-9][0-9]{2}$/
