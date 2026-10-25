import {
  Combine,
  Scissors,
  FileOutput,
  RotateCw,
  Image as ImageIcon,
  Images,
  FileText,
  Droplets,
  Unlock,
  Minimize2,
} from "lucide-react";

const ICONS = {
  Combine,
  Scissors,
  FileOutput,
  RotateCw,
  ImageIcon,
  Images,
  FileText,
  Droplets,
  Unlock,
  Minimize2,
};

export default function Icon({ name, className, ...props }) {
  const Cmp = ICONS[name] || FileText;
  return <Cmp className={className} {...props} />;
}
