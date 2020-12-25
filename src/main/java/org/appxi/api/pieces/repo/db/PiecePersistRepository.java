package org.appxi.api.pieces.repo.db;

import org.appxi.api.pieces.model.Piece;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PiecePersistRepository extends CrudRepository<Piece, String> {
}
