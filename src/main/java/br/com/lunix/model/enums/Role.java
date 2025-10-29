package br.com.lunix.model.enums;


/*
    Enum utilizado para definir as Roles(cargos) possíveis que um usuário pode ter
    na aplicação.

    ROLE_USER - Usuário comum que têm somente permissões de leitura(read) e de criação de
    avaliações para jogos.

    ROLE_ADMIN - ADMIN possuí permissão total a todos os sistemas na aplicação e áreas únicas para o mesmo.

    ROLE_DEV - Devs possuem permissões de usuários comuns e a capacidade de inserir, deletar e editar jogos
    que eles criaram, além de possuir áreas próprias para o mesmo.
*/
public enum Role {
    ROLE_USER,
    ROLE_ADMIN,
    ROLE_DEV
}
