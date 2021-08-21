export interface NavItem {
  label: string;
  subLabel?: string;
  children?: Array<NavItem>;
  href?: string;
}

const NAV_ITEMS: Array<NavItem> = [
  {
    label: 'Test',
    href: '/test',
  },
  {
    label: 'Config',
    href: '/config',
  },
  {
    label: 'Tools',
    href: '/tools',
  },
];

export default NAV_ITEMS;
