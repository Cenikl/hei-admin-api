do
$$
    begin
        if not exists(select from pg_type where typname = 'supervisor_type') then
            create type "supervisor_type" as enum ('TEACHER', 'ADMINISTRATOR');
        end if;
        if not exists(select from pg_type where typname = 'status_type') then
            create type "status_type" as enum ('END', 'EXPECTED', 'CANCELLED');
        end if;
    end
$$;
create table if not exists "event"
(
    id                      varchar
        constraint event_pk primary key default uuid_generate_v4(),
    description             varchar                  not null,
    place_name              varchar                  not null,
    start_event_datetime    timestamp with time zone not null,
    end_event_datetime      timestamp with time zone not null,
    supervisor              supervisor_type         not null,
    status                  status_type              not null
);
