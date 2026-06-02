export function formatCpfCnpj(value: string): string {
  const digits = value.replace(/\D/g, '')
  if (digits.length <= 11) {
    return digits
      .replace(/(\d{3})(\d)/, '$1.$2')
      .replace(/(\d{3})(\d)/, '$1.$2')
      .replace(/(\d{3})(\d{1,2})$/, '$1-$2')
  }
  return digits
    .replace(/(\d{2})(\d)/, '$1.$2')
    .replace(/(\d{3})(\d)/, '$1.$2')
    .replace(/(\d{3})(\d)/, '$1/$2')
    .replace(/(\d{4})(\d{1,2})$/, '$1-$2')
}

export function formatTelefone(value: string): string {
  const digits = value.replace(/\D/g, '')
  if (digits.length <= 10) {
    return digits
      .replace(/(\d{2})(\d)/, '($1) $2')
      .replace(/(\d{4})(\d)/, '$1-$2')
  }
  return digits
    .replace(/(\d{2})(\d)/, '($1) $2')
    .replace(/(\d{5})(\d)/, '$1-$2')
}

export function formatCep(value: string): string {
  const digits = value.replace(/\D/g, '')
  return digits.replace(/(\d{5})(\d)/, '$1-$2')
}

export function formatPlaca(value: string): string {
  return value.toUpperCase().replace(/[^A-Z0-9]/g, '').slice(0, 7)
}

export function formatDate(iso: string): string {
  return new Date(iso).toLocaleDateString('pt-BR')
}

export function stripMask(value: string): string {
  return value.replace(/\D/g, '')
}
