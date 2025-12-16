import os

# --- CONFIGURAÇÕES ---

# Nome do arquivo final que será gerado
OUTPUT_FILE = "projeto_completo.txt"

# Extensões de arquivos que serão lidas
EXTENSIONS_TO_INCLUDE = {
    # Java e Configs
    '.java', '.xml', '.properties', '.yml', '.yaml',
    # Web / Templates (caso use Thymeleaf/JSP)
    '.html', '.css', '.js',
    # Documentação e Build
    '.md', '.txt', '.sql', '.gradle', 'Dockerfile'
}

# Pastas que serão IGNORADAS (para não pegar lixo ou binários)
FOLDERS_TO_IGNORE = {
    'target', 'build', 'bin',         # Compilados
    '.git', '.idea', '.vscode',       # Configurações de IDE/Git
    '.settings', '.mvn', 'gradle',    # Configurações de Build/Wrapper
    'node_modules', '__pycache__',    # Outros
    '.files'
}

def is_text_file(filename):
    return any(filename.endswith(ext) for ext in EXTENSIONS_TO_INCLUDE)

def main():
    # Pega o diretório onde o script está sendo executado
    root_dir = os.getcwd()
    script_name = os.path.basename(__file__)

    print(f"--- Iniciando varredura em: {root_dir} ---")

    file_count = 0

    with open(OUTPUT_FILE, 'w', encoding='utf-8') as outfile:
        # Percorre a árvore de diretórios
        for current_root, dirs, files in os.walk(root_dir):
            # 1. Remove pastas ignoradas da lista de navegação
            # A alteração na lista 'dirs' afeta o os.walk, impedindo que ele entre nessas pastas
            dirs[:] = [d for d in dirs if d not in FOLDERS_TO_IGNORE]

            for file in files:
                # Evita que o script leia o próprio arquivo de saída ou o próprio script
                if file == OUTPUT_FILE or file == script_name:
                    continue

                if is_text_file(file):
                    file_path = os.path.join(current_root, file)
                    relative_path = os.path.relpath(file_path, root_dir)

                    try:
                        with open(file_path, 'r', encoding='utf-8', errors='ignore') as infile:
                            content = infile.read()

                            # Criação do cabeçalho visual para separar arquivos
                            header = (
                                f"\n{'='*60}\n"
                                f"CAMINHO: {relative_path}\n"
                                f"{'='*60}\n"
                            )

                            outfile.write(header)
                            outfile.write(content + "\n")

                            print(f"[OK] {relative_path}")
                            file_count += 1

                    except Exception as e:
                        print(f"[ERRO] Não foi possível ler {relative_path}: {e}")

    print(f"\n--- Concluído! ---")
    print(f"Total de arquivos exportados: {file_count}")
    print(f"Arquivo gerado: {os.path.join(root_dir, OUTPUT_FILE)}")

if __name__ == "__main__":
    main()