import { redirect } from "next/navigation";

// O proxy redireciona "/" conforme o perfil; isto é só um fallback.
export default function Home() {
  redirect("/login");
}
